package com.example.shoppinglistgenerator.model.repository

import android.util.Log
import com.example.shoppinglistgenerator.model.dto.ItemDto
import com.example.shoppinglistgenerator.model.dto.ShoppingListDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Egy bevásárlólista, amit a felhasználó láthat.
 *
 * - Saját lista: [pantryId] = null, a dokumentum ID-ja a felhasználó UID-ja.
 * - Kamra lista: [pantryId] = a kamra ID-ja, a dokumentum ID-ja "pantry_<pantryId>",
 *   a kamra minden tagja ugyanazt a listát látja.
 */
data class ShoppingListOption(
    val listId: String,
    val pantryId: String?,
    val name: String
) {
    val isPersonal: Boolean get() = pantryId == null

    companion object {
        fun personal(userId: String) = ShoppingListOption(userId, null, "Saját")
        fun pantry(pantryId: String, pantryName: String) =
            ShoppingListOption("pantry_$pantryId", pantryId, pantryName)
    }
}

class ShoppingListRepository(
    private val db: FirebaseFirestore = Firebase.firestore,
    private val itemRepository: ItemRepository = ItemRepository { db },
    private val userRepository: UserRepository = UserRepository(db),
    private val pantryRepository: PantryRepository = PantryRepository { db }
) {
    private val collection = db.collection("shopping_lists")
    private val itemsCollection = db.collection("items")

    /**
     * A felhasználó által elérhető listák: mindig az első a saját lista, utána a kamrák listái.
     * Ha a kamrák nem tölthetők be (pl. offline), csak a saját lista jelenik meg.
     */
    suspend fun getAvailableLists(userId: String): List<ShoppingListOption> {
        val pantryIds = userRepository.getUser(userId).getOrNull()?.pantries.orEmpty()
            .filter { it.isNotBlank() }
            .distinct()
        val pantries = pantryRepository.getPantriesByIds(pantryIds).getOrDefault(emptyList())
        return listOf(ShoppingListOption.personal(userId)) +
                pantries.map { ShoppingListOption.pantry(it.pantryId, it.name) }
    }

    /**
     * Tételek hozzáadása egy listához (ha még nincs ilyen lista, létrejön).
     * Ha a listán már van ugyanilyen nevű és mértékegységű, még nem kipipált tétel,
     * annak a mennyisége nő, különben új tétel kerül a listára.
     *
     * Offline is működik: ha a meglévő tételek nem olvashatók (pl. nincsenek a cache-ben),
     * az összevonás kimarad, és új tételek jönnek létre. Az írás nem várja meg a szerver
     * visszaigazolását: a Firestore helyben azonnal alkalmazza, és online állapotban feltölti.
     */
    suspend fun addItemsToList(
        list: ShoppingListOption,
        userId: String,
        newItems: List<ItemDto>,
        recipeIds: List<String> = emptyList()
    ): Result<Unit> = runCatching {
        val listRef = collection.document(list.listId)
        val existingIds = runCatching { listRef.get().await() }
            .onFailure { Log.w(TAG, "A lista nem olvasható (offline?), összevonás nélkül", it) }
            .getOrNull()
            ?.toObject(ShoppingListDto::class.java)
            ?.items
            .orEmpty()
        val openItemsByKey = itemRepository.getItemsByIds(existingIds)
            .onFailure { Log.w(TAG, "A tételek nem olvashatók (offline?), összevonás nélkül", it) }
            .getOrDefault(emptyList())
            .filter { !it.isPurchased }
            .associateBy { itemKey(it.name, it.unit) }

        val batch = db.batch()
        val createdIds = mutableListOf<String>()

        // Az azonos alapanyagokat előbb összevonjuk, hogy egy hozzáadás ne hozzon létre duplikátumot.
        newItems
            .groupBy { itemKey(it.name, it.unit) }
            .forEach { (key, group) ->
                val quantity = group.sumOf { it.quantity }
                val existing = openItemsByKey[key]
                if (existing != null) {
                    batch.update(
                        itemsCollection.document(existing.itemId),
                        "quantity", FieldValue.increment(quantity)
                    )
                } else {
                    val itemRef = itemsCollection.document()
                    batch.set(
                        itemRef,
                        group.first().copy(
                            itemId = itemRef.id,
                            quantity = quantity,
                            pantry = "",
                            isPurchased = false
                        )
                    )
                    createdIds += itemRef.id
                }
            }

        val listFields = mutableMapOf<String, Any>()
        if (list.pantryId != null) {
            listFields["pantry"] = list.pantryId
        } else {
            listFields["creator"] = userId
        }
        if (createdIds.isNotEmpty()) {
            listFields["items"] = FieldValue.arrayUnion(*createdIds.toTypedArray())
        }
        if (recipeIds.isNotEmpty()) {
            listFields["recipes"] = FieldValue.arrayUnion(*recipeIds.toTypedArray())
        }
        batch.set(listRef, listFields, SetOptions.merge())

        // Nem várunk a szerverre: a commit() hívás után a változás helyben már látszik,
        // a feltöltés a háttérben (offline esetén a kapcsolat visszatérésekor) történik.
        batch.commit()
            .addOnFailureListener { Log.e(TAG, "A lista mentése a szerverre nem sikerült", it) }
    }

    /** Tétel törlése a listáról (ID eltávolítása a tömbből + az item dokumentum törlése). */
    suspend fun removeItem(listId: String, itemId: String): Result<Unit> = runCatching {
        val batch = db.batch()
        batch.update(collection.document(listId), "items", FieldValue.arrayRemove(itemId))
        batch.delete(itemsCollection.document(itemId))
        batch.commit().await()
    }

    /** Valós idejű figyelés egy listára. null = még nincs ilyen lista (üresnek számít). */
    fun observeList(listId: String): Flow<ShoppingListDto?> =
        collection.document(listId)
            .snapshotFlow()
            .map { if (it.exists()) it.toObject(ShoppingListDto::class.java) else null }

    private fun itemKey(name: String, unit: String) =
        name.trim().lowercase() to unit.trim().lowercase()

    private companion object {
        const val TAG = "ShoppingListRepository"
    }
}
