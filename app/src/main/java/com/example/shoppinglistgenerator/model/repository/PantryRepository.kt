package com.example.shoppinglistgenerator.model.repository

import com.example.shoppinglistgenerator.model.dto.PantryDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class PantryRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("pantries")

    // Ide jönnek majd a lekérdezések és módosítások
}