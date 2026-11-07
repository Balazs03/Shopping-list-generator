package com.example.shoppinglistgenerator.model.repository

import com.example.shoppinglistgenerator.model.dto.ItemDto
import com.example.shoppinglistgenerator.model.dto.ShoppingListDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ShoppingListRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("shopping_lists")
    private val itemsCollection = db.collection("items")

    /**
     * Létrehoz egy bevásárlólistát a hiányzó tételekkel.
     * A tételek új dokumentumként kerülnek az "items" gyűjteménybe (pantry = ""),
     * a lista pedig az ID-jukat tárolja. Az egész egyetlen atomi batch-ben történik.
     *
     * @return az új lista ID-ja
     */
    suspend fun createShoppingList(
        pantryId: String,
        creatorId: String,
        recipeIds: List<String>,
        newItems: List<ItemDto>
    ): Result<String> = runCatching {
        val batch = db.batch()

        val itemIds = newItems.map { item ->
            val itemRef = itemsCollection.document()
            batch.set(itemRef, item.copy(itemId = itemRef.id, pantry = ""))
            itemRef.id
        }

        val listRef = collection.document()
        batch.set(
            listRef,
            ShoppingListDto(
                listId = listRef.id,
                pantry = pantryId,
                creator = creatorId,
                items = itemIds,
                recipes = recipeIds
            )
        )

        batch.commit().await()
        listRef.id
    }

    /** Új tétel felvétele egy meglévő listára (új item dokumentum + ID a lista items tömbjébe). */
    suspend fun addItem(listId: String, item: ItemDto): Result<Unit> = runCatching {
        val itemRef = itemsCollection.document()
        val batch = db.batch()
        batch.set(itemRef, item.copy(itemId = itemRef.id, pantry = ""))
        batch.update(collection.document(listId), "items", FieldValue.arrayUnion(itemRef.id))
        batch.commit().await()
    }

    /** Tétel törlése a listáról (ID eltávolítása a tömbből + az item dokumentum törlése). */
    suspend fun removeItem(listId: String, itemId: String): Result<Unit> = runCatching {
        val batch = db.batch()
        batch.update(collection.document(listId), "items", FieldValue.arrayRemove(itemId))
        batch.delete(itemsCollection.document(itemId))
        batch.commit().await()
    }

    /**
     * Valós idejű figyelés egy listára. null = a lista nem létezik.
     * Ha a dokumentum még nincs a helyi cache-ben, nem emittálunk "nem létezik" értéket,
     * nehogy offline indításkor hibásan "törölt listát" jelezzünk.
     */
    fun observeList(listId: String): Flow<ShoppingListDto?> =
        collection.document(listId)
            .snapshotFlow()
            .filter { it.exists() || !it.metadata.isFromCache }
            .map { it.toObject(ShoppingListDto::class.java) }
}