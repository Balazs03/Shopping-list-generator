package com.example.shoppinglistgenerator.model.repository

import com.example.shoppinglistgenerator.model.dto.RecipeDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class RecipeRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("recipes")

    /** A globális recepttár összes receptje. */
    suspend fun getAllRecipes(): Result<List<RecipeDto>> = runCatching {
        collection.get().await().toObjects(RecipeDto::class.java)
    }
}