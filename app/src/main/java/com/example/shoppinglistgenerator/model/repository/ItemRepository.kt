package com.example.shoppinglistgenerator.model.repository

import com.example.shoppinglistgenerator.model.dto.ItemDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

open class ItemRepository(
    private val dbProvider: () -> FirebaseFirestore = { Firebase.firestore }
) {
    private val db by lazy { dbProvider() }
    private val collection get() = db.collection("items")

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
}