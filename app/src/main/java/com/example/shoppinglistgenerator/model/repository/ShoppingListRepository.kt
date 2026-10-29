package com.example.shoppinglistgenerator.model.repository

import com.example.shoppinglistgenerator.model.dto.ShoppingListDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class ShoppingListRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("shopping_lists")

    // Ide jönnek majd a lekérdezések és módosítások
}