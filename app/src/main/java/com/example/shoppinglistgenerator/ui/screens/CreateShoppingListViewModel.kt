package com.example.shoppinglistgenerator.ui.screens

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.shoppinglistgenerator.model.dto.ItemDto
import com.example.shoppinglistgenerator.model.dto.PantryDto
import com.example.shoppinglistgenerator.model.dto.RecipeDto
import com.example.shoppinglistgenerator.model.repository.AuthRepository
import com.example.shoppinglistgenerator.model.repository.ItemRepository
import com.example.shoppinglistgenerator.model.repository.PantryRepository
import com.example.shoppinglistgenerator.model.repository.RecipeRepository
import com.example.shoppinglistgenerator.model.repository.ShoppingListRepository
import com.example.shoppinglistgenerator.model.repository.UserRepository
import com.example.shoppinglistgenerator.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/** Egy recept a kiválasztó listában. */
data class RecipeSelectUi(
    val id: String,
    val name: String,
    val ingredientCount: Int
)

/** Egy választható kamra (saját vagy olyan, amihez a felhasználó csatlakozott). */
data class PantryOptionUi(
    val id: String,
    val name: String,
    val memberCount: Int
)

/** A "Készlet összetétele" táblázat egy sora. */
data class IngredientRowUi(
    val name: String,
    val unit: String,
    val needed: Double,
    val inPantry: Double,
    val pantryUnit: String,
    /** 0, ha a kamrában elegendő mennyiség van. */
    val missing: Double
)

data class CreateShoppingListUiState(
    val isLoading: Boolean = true,
    val recipes: List<RecipeSelectUi> = emptyList(),
    val selectedRecipeIds: Set<String> = emptySet(),
    val pantries: List<PantryOptionUi> = emptyList(),
    val selectedPantryId: String? = null,
    val rows: List<IngredientRowUi> = emptyList(),
    val isCreating: Boolean = false,
    val errorMessage: String? = null,
    /** Ha nem null, a lista elkészült, a screen erre navigál. */
    val createdListId: String? = null
) {
    val missingRows: List<IngredientRowUi>
        get() = rows.filter { it.missing > 0.0 }

    val canCreate: Boolean
        get() = selectedRecipeIds.isNotEmpty() && missingRows.isNotEmpty() && !isCreating
}

class CreateShoppingListViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.CreateShoppingList>()

    private val recipeRepository = RecipeRepository()
    private val itemRepository = ItemRepository()
    private val pantryRepository = PantryRepository()
    private val shoppingListRepository = ShoppingListRepository()
    private val userRepository = UserRepository()
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(CreateShoppingListUiState())
    val uiState: StateFlow<CreateShoppingListUiState> = _uiState.asStateFlow()

    private var loadedRecipes: List<RecipeDto> = emptyList()
    private var pantriesById: Map<String, PantryDto> = emptyMap()

    /** Receptek hozzávalói (item ID -> item), igény szerint töltve. */
    private val itemCache = mutableMapOf<String, ItemDto>()

    /** Kamrák készlete (pantry ID -> elemek), igény szerint töltve. */
    private val pantryItemsCache = mutableMapOf<String, List<ItemDto>>()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val recipesResult = recipeRepository.getAllRecipes()
            loadedRecipes = recipesResult.getOrDefault(emptyList())

            // A kamrák: a felhasználó saját kamrái + a navigációból érkező kamra.
            // Ha a bejelentkezés most nem sikerül, a navigációból kapott kamrával dolgozunk tovább.
            val uid = runCatching { authRepository.ensureAuthenticated() }.getOrNull()
            val userPantryIds = uid
                ?.let { userRepository.getUser(it).getOrNull()?.pantries }
                .orEmpty()
            val pantryIds = (userPantryIds + route.pantryId.orEmpty())
                .filter { it.isNotBlank() }
                .distinct()
            val pantries = pantryRepository.getPantriesByIds(pantryIds).getOrDefault(emptyList())
            pantriesById = pantries.associateBy { it.pantryId }

            val selectedPantryId = pantries.firstOrNull { it.pantryId == route.pantryId }?.pantryId
                ?: pantries.firstOrNull()?.pantryId
            selectedPantryId?.let { loadPantryItems(it) }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    recipes = loadedRecipes.map { r -> RecipeSelectUi(r.recipeId, r.name, r.items.size) },
                    pantries = pantries.map { p -> PantryOptionUi(p.pantryId, p.name, p.users.size) },
                    selectedPantryId = selectedPantryId,
                    errorMessage = if (recipesResult.isFailure) "A receptek betöltése nem sikerült." else null
                )
            }
        }
    }

    fun onRecipeToggled(recipeId: String) {
        val nowSelected = recipeId !in _uiState.value.selectedRecipeIds
        _uiState.update {
            it.copy(
                selectedRecipeIds = if (nowSelected) {
                    it.selectedRecipeIds + recipeId
                } else {
                    it.selectedRecipeIds - recipeId
                }
            )
        }
        if (nowSelected) {
            viewModelScope.launch {
                loadRecipeItems(recipeId)
                recompute()
            }
        } else {
            recompute()
        }
    }

    fun onPantrySelected(pantryId: String) {
        if (pantryId == _uiState.value.selectedPantryId) return
        _uiState.update { it.copy(selectedPantryId = pantryId) }
        viewModelScope.launch {
            loadPantryItems(pantryId)
            recompute()
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onCreateClick() {
        val state = _uiState.value
        if (!state.canCreate) return

        _uiState.update { it.copy(isCreating = true, errorMessage = null) }

        viewModelScope.launch {
            runCatching {
                // Offline módban a Firestore írás várakozna, ezért időkorlát van rajta.
                withTimeout(CREATE_TIMEOUT_MS) {
                    val creatorId = authRepository.ensureAuthenticated()
                    // Csak a ténylegesen hiányzó mennyiségek kerülnek a listára.
                    val newItems = state.missingRows.map {
                        ItemDto(
                            name = it.name,
                            unit = it.unit,
                            quantity = it.missing,
                            pantry = "",
                            isPurchased = false
                        )
                    }
                    shoppingListRepository.createShoppingList(
                        pantryId = state.selectedPantryId.orEmpty(),
                        creatorId = creatorId,
                        recipeIds = state.selectedRecipeIds.toList(),
                        newItems = newItems
                    ).getOrThrow()
                }
            }.onSuccess { listId ->
                _uiState.update { it.copy(isCreating = false, createdListId = listId) }
            }.onFailure { e ->
                Log.e(TAG, "Létrehozás sikertelen", e)
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        errorMessage = "A lista létrehozása nem sikerült (${e::class.simpleName})."
                    )
                }
            }
        }
    }

    private suspend fun loadPantryItems(pantryId: String) {
        if (pantryItemsCache.containsKey(pantryId)) return
        val ids = pantriesById[pantryId]?.items.orEmpty()
        itemRepository.getItemsByIds(ids)
            .onSuccess { pantryItemsCache[pantryId] = it }
            .onFailure { showError("A kamra készletének betöltése nem sikerült.") }
    }

    private suspend fun loadRecipeItems(recipeId: String) {
        val missingIds = loadedRecipes
            .firstOrNull { it.recipeId == recipeId }
            ?.items
            .orEmpty()
            .filter { it !in itemCache }
        if (missingIds.isEmpty()) return
        itemRepository.getItemsByIds(missingIds)
            .onSuccess { items -> items.forEach { itemCache[it.itemId] = it } }
            .onFailure { showError("A hozzávalók betöltése nem sikerült.") }
    }

    private fun recompute() {
        _uiState.update {
            it.copy(rows = buildRows(it.selectedRecipeIds, it.selectedPantryId))
        }
    }

    /**
     * Üzleti logika: a kiválasztott receptek hozzávalóit (azonos név + mértékegység szerint
     * összevonva) összeveti a kamra készletével, és kiszámolja a hiányzó mennyiséget.
     *
     * Ha a kamrában ugyanaz az alapanyag MÁS mértékegységben van (pl. kg vs g, db vs kg),
     * a mennyiségek nem hasonlíthatók össze, ezért a teljes szükséges mennyiség hiányzónak számít.
     * A "több mértékegység" kérdés eldöntése után itt kell finomítani.
     */
    private fun buildRows(selectedRecipeIds: Set<String>, pantryId: String?): List<IngredientRowUi> {
        val selectedRecipes = loadedRecipes.filter { it.recipeId in selectedRecipeIds }
        val needed = selectedRecipes.flatMap { recipe -> recipe.items.mapNotNull { itemCache[it] } }
        val pantryItems = pantryId?.let { pantryItemsCache[it] }.orEmpty()

        return needed
            .groupBy { normalize(it.name) to normalize(it.unit) }
            .map { (key, group) ->
                val first = group.first()
                val total = group.sumOf { it.quantity }
                val sameName = pantryItems.filter { normalize(it.name) == key.first }
                val sameUnit = sameName.filter { normalize(it.unit) == key.second }
                val have = sameUnit.sumOf { it.quantity }
                val shown = if (sameUnit.isNotEmpty()) sameUnit else sameName

                IngredientRowUi(
                    name = first.name,
                    unit = first.unit,
                    needed = total,
                    inPantry = shown.sumOf { it.quantity },
                    pantryUnit = shown.firstOrNull()?.unit ?: first.unit,
                    missing = roundAmount((total - have).coerceAtLeast(0.0))
                )
            }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    private fun normalize(text: String) = text.trim().lowercase()

    private fun roundAmount(value: Double) = Math.round(value * 1000) / 1000.0

    private companion object {
        const val TAG = "CreateShoppingList"
        const val CREATE_TIMEOUT_MS = 15_000L
    }
}