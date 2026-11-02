package com.example.shoppinglistgenerator.model.repository

import com.example.shoppinglistgenerator.model.dto.PantryDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

// Egy felhasználó legfeljebb ennyi kamrához tartozhat
const val MAX_PANTRIES_PER_USER = 2

// A csatlakozás üzleti okokból elutasított esetei (a hálózati hibák külön jönnek)
sealed class PantryJoinException : Exception() {
    class NotFound : PantryJoinException()
    class AlreadyMember : PantryJoinException()
    class LimitReached : PantryJoinException()
}

class PantryRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("pantries")

    // Valós idejű kamra adatok (név, tagok); null, ha a dokumentum nem létezik
    fun observePantry(pantryId: String): Flow<PantryDto?> =
        collection.document(pantryId).snapshots().map { it.toObject<PantryDto>() }

    // Csatlakozás egy meglévő kamrához a jelkódja (password mező) alapján
    suspend fun joinPantry(code: String, uid: String): Result<PantryDto> = runCatching {
        val pantryDoc = collection.whereEqualTo("password", code).limit(1).get().await()
            .documents.firstOrNull() ?: throw PantryJoinException.NotFound()
        val pantryRef = pantryDoc.reference
        val userRef = db.collection("users").document(uid)

        db.runTransaction { transaction ->
            val pantry = transaction.get(pantryRef).toObject<PantryDto>()
                ?: throw PantryJoinException.NotFound()
            val userPantries = transaction.get(userRef).get("pantries") as? List<*> ?: emptyList<Any>()

            if (uid in pantry.users || pantry.pantryId in userPantries) {
                throw PantryJoinException.AlreadyMember()
            }
            if (userPantries.size >= MAX_PANTRIES_PER_USER) {
                throw PantryJoinException.LimitReached()
            }

            transaction.update(pantryRef, "users", FieldValue.arrayUnion(uid))
            // merge: akkor is működjön, ha a user dokumentum még nem jött létre
            transaction.set(
                userRef,
                mapOf("pantries" to FieldValue.arrayUnion(pantry.pantryId)),
                SetOptions.merge()
            )
            pantry
        }.await()
    }
}
