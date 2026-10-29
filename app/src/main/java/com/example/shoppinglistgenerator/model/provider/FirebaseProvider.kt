package com.example.shoppinglistgenerator.model.provider

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings

object FirebaseProvider {
    val db by lazy {
        Firebase.firestore.apply {
            firestoreSettings = firestoreSettings {
                isPersistenceEnabled = true // Offline adatmentés engedélyezése
            }
        }
    }
}