package com.example.shoppinglistgenerator.model.repository

import com.example.shoppinglistgenerator.model.dto.PantryDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

open class PantryRepository(
    private val dbProvider: () -> FirebaseFirestore = { Firebase.firestore }
) {
    private val db by lazy { dbProvider() }
    private val collection get() = db.collection("pantries")

    suspend fun getPantryById(pantryId: String): Result<PantryDto?> = runCatching {
        if (pantryId.isBlank()) return@runCatching null
        val doc = collection.document(pantryId).get().await()
        doc.toObject(PantryDto::class.java)
    }

    suspend fun getPantriesByIds(pantryIds: List<String>): Result<List<PantryDto>> = runCatching {
        if (pantryIds.isEmpty()) return@runCatching emptyList()
        val pantries = mutableListOf<PantryDto>()
        for (id in pantryIds) {
            val doc = collection.document(id).get().await()
            val dto = doc.toObject(PantryDto::class.java)
            if (dto != null) {
                pantries.add(dto)
            }
        }
        pantries
    }
}