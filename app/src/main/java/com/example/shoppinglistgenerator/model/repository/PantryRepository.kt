package com.example.shoppinglistgenerator.model.repository

import com.example.shoppinglistgenerator.model.dto.PantryDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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

open class PantryRepository(
    private val dbProvider: () -> FirebaseFirestore = { Firebase.firestore }
) {
    private val db by lazy { dbProvider() }
    private val collection get() = db.collection("pantries")

    // Valós idejű kamra adatok (név, tagok); null, ha a dokumentum nem létezik
    fun observePantry(pantryId: String): Flow<PantryDto?> =
        collection.document(pantryId).snapshots().map { it.toObject<PantryDto>() }

    // Valós idejű kamra lista egy felhasználóhoz (a default personal kamra kerül előre)
    fun observePantriesForUser(uid: String): Flow<List<PantryDto>> {
        val userRef = db.collection("users").document(uid)
        return combine(
            userRef.snapshots(),
            collection.whereArrayContains("users", uid).snapshots()
        ) { _, pantriesSnap ->
            val pantriesFromQuery = pantriesSnap.toObjects(PantryDto::class.java)
            val pantriesMap = pantriesFromQuery.associateBy { it.pantryId }.toMutableMap()

            pantriesMap.values.map { pantry ->
                val isDefault = pantry.isDefaultPersonal || pantry.name == "Saját kamra" ||
                        (pantry.creator == uid && pantry.password.isBlank() && pantry.users.size <= 1)
                pantry.copy(isDefaultPersonal = isDefault)
            }.sortedWith(
                compareByDescending<PantryDto> { it.isDefaultPersonal }
                    .thenBy { it.name }
            )
        }
    }

    // Biztosítja, hogy a felhasználónak legyen legalább egy default personal kamrája ("Saját kamra")
    suspend fun ensureDefaultPersonalPantry(uid: String): Result<PantryDto> = runCatching {
        val userRef = db.collection("users").document(uid)
        val userPantriesSnap = collection.whereArrayContains("users", uid).get().await()
        val pantries = userPantriesSnap.toObjects(PantryDto::class.java)

        val existingDefault = pantries.firstOrNull {
            it.isDefaultPersonal || it.name == "Saját kamra" || (it.creator == uid && it.password.isBlank())
        }

        if (existingDefault != null) {
            if (!existingDefault.isDefaultPersonal) {
                val pantryRef = collection.document(existingDefault.pantryId)
                pantryRef.set(
                    mapOf("defaultPersonal" to true, "isDefaultPersonal" to true),
                    SetOptions.merge()
                )
            }
            return@runCatching existingDefault.copy(isDefaultPersonal = true)
        }

        val newPantryRef = collection.document()
        val defaultPantry = PantryDto(
            pantryId = newPantryRef.id,
            name = "Saját kamra",
            creator = uid,
            password = "",
            isDefaultPersonal = true,
            users = listOf(uid)
        )

        val batch = db.batch()
        batch.set(newPantryRef, defaultPantry)
        batch.set(
            userRef,
            mapOf("userId" to uid, "pantries" to FieldValue.arrayUnion(newPantryRef.id)),
            SetOptions.merge()
        )
        batch.commit().await()
        defaultPantry
    }

    // Új közös kamra létrehozása (Offline-first: azonnal menti a helyi cache-be)
    suspend fun createPantry(name: String, joinCode: String, uid: String): Result<PantryDto> = runCatching {
        val newPantryRef = collection.document()
        val newPantry = PantryDto(
            pantryId = newPantryRef.id,
            name = name,
            creator = uid,
            password = joinCode,
            isDefaultPersonal = false,
            users = listOf(uid)
        )

        val batch = db.batch()
        batch.set(newPantryRef, newPantry)
        val userRef = db.collection("users").document(uid)
        batch.set(
            userRef,
            mapOf("pantries" to FieldValue.arrayUnion(newPantryRef.id)),
            SetOptions.merge()
        )
        // Nem várjuk meg a hálózati .await()-et, hogy azonnal visszatérjen (offline-first)
        batch.commit()
        newPantry
    }

    // Közös kamra törlése (csak tulajdonos törölheti, a default personal kamra nem törölhető)
    suspend fun deleteSharedPantry(pantryId: String, uid: String): Result<Unit> = runCatching {
        val pantryRef = collection.document(pantryId)
        val pantryDoc = pantryRef.get().await()
        val pantry = pantryDoc.toObject(PantryDto::class.java)
            ?: throw PantryJoinException.NotFound()

        if (pantry.isDefaultPersonal) {
            throw IllegalStateException("A saját alapértelmezett kamra nem törölhető.")
        }
        if (pantry.creator != uid) {
            throw IllegalStateException("Csak a tulajdonos törölheti a kamrát.")
        }

        val batch = db.batch()
        batch.delete(pantryRef)
        for (memberUid in pantry.users) {
            val userRef = db.collection("users").document(memberUid)
            batch.set(
                userRef,
                mapOf("pantries" to FieldValue.arrayRemove(pantryId)),
                SetOptions.merge()
            )
        }
        batch.commit().await()
    }

    // Kilépés közös kamrából (a default personal kamrából nem lehet kilépni)
    suspend fun leaveSharedPantry(pantryId: String, uid: String): Result<Unit> = runCatching {
        val pantryRef = collection.document(pantryId)
        val pantryDoc = pantryRef.get().await()
        val pantry = pantryDoc.toObject(PantryDto::class.java)
            ?: throw PantryJoinException.NotFound()

        if (pantry.isDefaultPersonal) {
            throw IllegalStateException("A saját alapértelmezett kamrából nem lehet kilépni.")
        }

        val userRef = db.collection("users").document(uid)
        val batch = db.batch()
        batch.update(pantryRef, "users", FieldValue.arrayRemove(uid))
        batch.set(
            userRef,
            mapOf("pantries" to FieldValue.arrayRemove(pantryId)),
            SetOptions.merge()
        )
        batch.commit().await()
    }

    // Csatlakozás egy meglévő kamrához a jelkódja (password mező) alapján
    suspend fun joinPantry(code: String, uid: String): Result<PantryDto> = runCatching {
        val pantryQuery = collection.whereEqualTo("password", code).limit(1).get().await()
        val pantryDoc = pantryQuery.documents.firstOrNull() ?: throw PantryJoinException.NotFound()
        val pantryRef = pantryDoc.reference
        val userRef = db.collection("users").document(uid)

        db.runTransaction { transaction ->
            val pantry = transaction.get(pantryRef).toObject<PantryDto>()
                ?: throw PantryJoinException.NotFound()
            val userPantries = transaction.get(userRef).get("pantries") as? List<*> ?: emptyList<Any>()

            if (uid in pantry.users || pantry.pantryId in userPantries) {
                throw PantryJoinException.AlreadyMember()
            }
            // Limit ellenőrzés kiszedve/kikommentelve, mivel a UI már letiltja a gombot
            // if (userPantries.size >= MAX_PANTRIES_PER_USER) {
            //     throw PantryJoinException.LimitReached()
            // }

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

    suspend fun getPantryById(pantryId: String): Result<PantryDto?> = runCatching {
        if (pantryId.isBlank()) return@runCatching null
        val doc = collection.document(pantryId).get().await()
        doc.toObject(PantryDto::class.java)
    }

    suspend fun getPantriesByIds(pantryIds: List<String>): Result<List<PantryDto>> = runCatching {
        if (pantryIds.isEmpty()) return@runCatching emptyList()
        val pantries = mutableListOf<PantryDto>()
        for (id in pantryIds) {
            val doc = collection.document(id).get().await()
            val dto = doc.toObject(PantryDto::class.java)
            if (dto != null) {
                pantries.add(dto)
            }
        }
        pantries
    }
}