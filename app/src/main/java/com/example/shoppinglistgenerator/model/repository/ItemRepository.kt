package com.example.shoppinglistgenerator.model.repository

import com.example.shoppinglistgenerator.model.dto.ItemDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class ItemRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("items")

    // Ide jönnek majd az lekérdezések és módosítások
}