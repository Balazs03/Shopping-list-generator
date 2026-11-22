package com.example.shoppinglistgenerator.model.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = Firebase.auth,
    private val userRepository: UserRepository = UserRepository(),
    private val pantryRepository: PantryRepository = PantryRepository()
) {
    private var isDefaultPantryEnsured = false

    // Automatikus, csendes bejelentkezés / regisztráció
    suspend fun ensureAuthenticated(): String {
        val currentUser = auth.currentUser
        
        val uid = if (currentUser != null) {
            // Már van fiókja, visszaadjuk az UID-t
            currentUser.uid
        } else {
            // Első indítás: névtelen fiók létrehozása a háttérben
            val authResult = auth.signInAnonymously().await()
            val newUid = authResult.user?.uid ?: throw Exception("Auth failed")
            
            // Kezdő User rekord létrehozása a Firestore 'users' gyűjteményben
            userRepository.createUserProfile(newUid)
            runCatching { pantryRepository.ensureDefaultPersonalPantry(newUid) }
            isDefaultPantryEnsured = true
            newUid
        }

        if (!isDefaultPantryEnsured) {
            isDefaultPantryEnsured = true
            // Háttérben ellenőrizzük, hogy megvan-e a Saját kamra, így nem blokkoljuk a UI-t
            CoroutineScope(Dispatchers.IO).launch {
                runCatching { pantryRepository.ensureDefaultPersonalPantry(uid) }
            }
        }

        return uid
    }
}