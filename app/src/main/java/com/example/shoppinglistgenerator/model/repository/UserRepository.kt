package com.example.shoppinglistgenerator.model.repository

import com.example.shoppinglistgenerator.model.dto.UserDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class UserRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("users")

    // Ide jönnek majd a lekérdezések és módosítások
}