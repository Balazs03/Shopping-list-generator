package com.example.shoppinglistgenerator.model.repository

import com.example.shoppinglistgenerator.model.dto.UserDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("users")

    // Új felhasználói profil létrehozása az első belépéskor
    suspend fun createUserProfile(uid: String, defaultName: String = "Névtelen felhasználó"): Result<Unit> = runCatching {
        val newUser = UserDto(
            userId = uid,
            name = defaultName,
            email = "", // Névtelen belépésnél még nincs e-mail cím
            pantries = emptyList() // Kezdetben még nem csatlakozott egyetlen kamrához sem
        )

        // A dokumentum ID-ja pontosan megegyezik az Auth UID-val
        collection.document(uid).set(newUser).await()
    }
}