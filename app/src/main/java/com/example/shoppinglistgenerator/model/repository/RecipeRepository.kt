package com.example.shoppinglistgenerator.model.repository

import com.example.shoppinglistgenerator.model.dto.RecipeDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class RecipeRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("recipes")

    // Ide jönnek majd a lekérdezések és módosítások
}