package com.example.shoppinglistgenerator.model.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = Firebase.auth,
    private val userRepository: UserRepository = UserRepository()
) {
    // Automatikus, csendes bejelentkezés / regisztráció
    suspend fun ensureAuthenticated(): String {
        val currentUser = auth.currentUser
        
        return if (currentUser != null) {
            // Már van névtelen fiókja, visszaadjuk az UID-t
            currentUser.uid
        } else {
            // Első indítás: névtelen fiók létrehozása a háttérben
            val authResult = auth.signInAnonymously().await()
            val newUid = authResult.user?.uid ?: throw Exception("Auth failed")
            
            // Opcionális: Kezdő User rekord létrehozása a Firestore 'users' gyűjteményben
            userRepository.createUserProfile(newUid)
            
            newUid
        }
    }
}