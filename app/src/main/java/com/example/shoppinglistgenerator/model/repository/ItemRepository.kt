package com.example.shoppinglistgenerator.model.repository

import com.example.shoppinglistgenerator.model.dto.ItemDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ItemRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("items")

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
}
