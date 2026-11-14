package com.example.shoppinglistgenerator.model.repository

import com.example.shoppinglistgenerator.model.dto.ItemDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

open class ItemRepository(
    private val dbProvider: () -> FirebaseFirestore = { Firebase.firestore }
) {
    private val db by lazy { dbProvider() }
    private val collection get() = db.collection("items")

    // Valós idejű lista egy kamra alapanyagairól, név szerint rendezve
    fun observeItemsByPantry(pantryId: String): Flow<List<ItemDto>> =
        collection.whereEqualTo("pantry", pantryId).snapshots().map { snapshot ->
            snapshot.toObjects<ItemDto>().sortedBy { it.name.lowercase() }
        }

    // Törli az alapanyagot, és kiveszi az ID-ját a kamra items tömbjéből (egy batch-ben)
    suspend fun deleteItem(item: ItemDto): Result<Unit> = runCatching {
        val batch = db.batch()
        batch.delete(collection.document(item.itemId))
        if (item.pantry.isNotBlank()) {
            batch.update(
                db.collection("pantries").document(item.pantry),
                "items", FieldValue.arrayRemove(item.itemId)
            )
        }
        batch.commit().await()
    }

    /**
     * Létrehoz egy új alapanyagot a Firestore-ban, és beállítja annak kamra azonosítóját (pantry),
     * valamint hozzáadja az elem ID-ját a kamra (pantry) items tömbjéhez.
     */
    open suspend fun createItem(item: ItemDto): Result<String> = runCatching {
        val docRef = collection.document()
        val newItem = item.copy(itemId = docRef.id)
        docRef.set(newItem).await()

        if (newItem.pantry.isNotBlank()) {
            db.collection("pantries")
                .document(newItem.pantry)
                .update("items", FieldValue.arrayUnion(docRef.id))
                .await()
        }

        docRef.id
    }

    /**
     * Kényelmi funkció alapanyag létrehozásához megadott mezők alapján.
     */
    open suspend fun createItem(
        name: String,
        unit: String,
        quantity: Double,
        pantry: String
    ): Result<String> {
        val item = ItemDto(
            name = name,
            unit = unit,
            quantity = quantity,
            pantry = pantry,
            isPurchased = false
        )
        return createItem(item)
    }

    /**
     * Egyszeri lekérdezés ID-k alapján (recept hozzávalók, kamra elemek).
     * A Firestore whereIn legfeljebb 30 értéket enged, ezért darabokra bontjuk.
     */
    suspend fun getItemsByIds(ids: List<String>): Result<List<ItemDto>> = runCatching {
        ids.distinct().chunked(FIRESTORE_IN_LIMIT).flatMap { chunk ->
            collection
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .await()
                .toObjects(ItemDto::class.java)
        }
    }

    /**
     * Valós idejű figyelés ID-k alapján (bevásárlólista tételei).
     * Offline is kiad értéket a helyi cache-ből.
     */
    fun observeItemsByIds(ids: List<String>): Flow<List<ItemDto>> {
        val chunks = ids.distinct().chunked(FIRESTORE_IN_LIMIT)
        if (chunks.isEmpty()) return flowOf(emptyList())

        val flows = chunks.map { chunk ->
            collection
                .whereIn(FieldPath.documentId(), chunk)
                .snapshotFlow()
                .map { it.toObjects(ItemDto::class.java) }
        }
        return combine(flows) { parts -> parts.toList().flatten() }
    }

    /**
     * Kipipálás / visszavonás. Offline módban a Firestore helyben azonnal alkalmazza,
     * a szerverre csak újra online állapotban kerül fel.
     */
    suspend fun setPurchased(itemId: String, purchased: Boolean): Result<Unit> = runCatching {
        collection.document(itemId).update("purchased", purchased).await()
    }

    /** Egy tétel nevének, mennyiségének és mértékegységének módosítása. */
    suspend fun updateItem(
        itemId: String,
        name: String,
        quantity: Double,
        unit: String
    ): Result<Unit> = runCatching {
        collection.document(itemId)
            .update(mapOf("name" to name, "quantity" to quantity, "unit" to unit))
            .await()
    }

    private companion object {
        const val FIRESTORE_IN_LIMIT = 30
    }
}
