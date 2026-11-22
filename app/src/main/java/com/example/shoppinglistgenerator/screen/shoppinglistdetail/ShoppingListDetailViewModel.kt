package com.example.shoppinglistgenerator.screen.shoppinglistdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoppinglistgenerator.model.dto.ItemDto
import com.example.shoppinglistgenerator.model.repository.AuthRepository
import com.example.shoppinglistgenerator.model.repository.ItemRepository
import com.example.shoppinglistgenerator.model.repository.ShoppingListOption
import com.example.shoppinglistgenerator.model.repository.ShoppingListRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Egy tétel a listanézetben, a címkéje megmutatja, melyik listához (Saját / kamra) tartozik. */
data class ShoppingListItemUi(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val purchased: Boolean,
    /** A tételt tartalmazó lista dokumentum ID-ja (törléshez). */
    val listId: String,
    /** A címke szövege: "Saját" vagy a kamra neve. */
    val tag: String
)

data class ShoppingListDetailUiState(
    val isLoading: Boolean = true,
    /** Saját lista + a kamrák listái. A szűrő csak akkor látszik, ha több van. */
    val lists: List<ShoppingListOption> = emptyList(),
    /** A szűrő: null = minden tétel, különben csak az adott lista tételei. */
    val selectedListId: String? = null,
    /** Az összes lista összes tétele, egyetlen listába fésülve. */
    val items: List<ShoppingListItemUi> = emptyList(),
    /** false = "Összes", true = "Hiányzó" (még nem kipipált) tételek. */
    val showOnlyRemaining: Boolean = false,
    val errorMessage: String? = null
) {
    val hasMultipleLists: Boolean
        get() = lists.size > 1

    val selectedList: ShoppingListOption?
        get() = lists.firstOrNull { it.listId == selectedListId }

    /** A lista szűrő (Mind / Saját / kamra) szerinti tételek. */
    val filteredItems: List<ShoppingListItemUi>
        get() = if (selectedListId == null) items else items.filter { it.listId == selectedListId }

    /** A lista szűrő és az Összes / Hiányzó szűrő szerinti tételek. */
    val visibleItems: List<ShoppingListItemUi>
        get() = if (showOnlyRemaining) filteredItems.filter { !it.purchased } else filteredItems

    val purchasedCount: Int
        get() = filteredItems.count { it.purchased }

    val progress: Float
        get() = if (filteredItems.isEmpty()) 0f else purchasedCount.toFloat() / filteredItems.size
}

/**
 * A "Lista" fül: egyetlen bevásárlólista, ami a saját és a kamrák tételeit együtt mutatja.
 * A tárolás továbbra is külön dokumentumokban történik, így a kamra tételeit a többi tag is látja.
 */
class ShoppingListDetailViewModel : ViewModel() {

    private val shoppingListRepository = ShoppingListRepository()
    private val itemRepository = ItemRepository()
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(ShoppingListDetailUiState())
    val uiState: StateFlow<ShoppingListDetailUiState> = _uiState.asStateFlow()

    private var userId: String? = null

    init {
        loadLists()
        observeAllLists()
    }

    private fun loadLists() {
        viewModelScope.launch {
            val uid = runCatching { authRepository.ensureAuthenticated() }.getOrElse {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "A bejelentkezés nem sikerült.")
                }
                return@launch
            }
            userId = uid
            val lists = shoppingListRepository.getAvailableLists(uid)
            _uiState.update { state ->
                state.copy(
                    lists = lists,
                    // Ha a korábban szűrt lista már nem elérhető, vissza a "Mind" nézetre.
                    selectedListId = state.selectedListId?.takeIf { id -> lists.any { it.listId == id } }
                )
            }
        }
    }

    /**
     * Valós idejű figyelés az összes listára egyszerre (Firestore snapshot listenerek),
     * a tételek egyetlen, címkézett listába fésülve.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAllLists() {
        viewModelScope.launch {
            _uiState
                .map { it.lists }
                .filter { it.isNotEmpty() }
                .distinctUntilChanged()
                .flatMapLatest { lists ->
                    combine(lists.map(::observeListItems)) { parts -> parts.toList().flatten() }
                }
                .collect { items ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            // A kipipált tételek a lista aljára kerülnek, a Figma szerint.
                            items = items.sortedWith(
                                compareBy<ShoppingListItemUi>({ ui -> ui.purchased }, { ui -> ui.name.lowercase() })
                            )
                        )
                    }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeListItems(list: ShoppingListOption): Flow<List<ShoppingListItemUi>> =
        shoppingListRepository.observeList(list.listId)
            .flatMapLatest { dto -> itemRepository.observeItemsByIds(dto?.items.orEmpty()) }
            .map { items -> items.map { it.toUi(list) } }
            .catch {
                showError("A(z) ${list.name} lista betöltése nem sikerült.")
                emit(emptyList())
            }

    /** Lista szűrő: null = minden tétel. */
    fun onListFilterChanged(listId: String?) {
        _uiState.update { it.copy(selectedListId = listId) }
    }

    fun onFilterChanged(showOnlyRemaining: Boolean) {
        _uiState.update { it.copy(showOnlyRemaining = showOnlyRemaining) }
    }

    /**
     * Kipipálás. A Firestore a helyi nézetet azonnal frissíti (a snapshot listener jelez),
     * a szerverre és a többi tagnak csak online állapotban kerül ki.
     */
    fun onItemChecked(itemId: String, checked: Boolean) {
        viewModelScope.launch {
            itemRepository.setPurchased(itemId, checked)
                .onFailure { showError("A módosítás mentése nem sikerült.") }
        }
    }

    /** Új tétel felvétele a megadott listára (Saját vagy kamra). */
    fun addItem(name: String, quantity: Double, unit: String, listId: String) {
        val list = _uiState.value.lists.firstOrNull { it.listId == listId } ?: return
        val uid = userId ?: return
        viewModelScope.launch {
            shoppingListRepository.addItemsToList(
                list = list,
                userId = uid,
                newItems = listOf(
                    ItemDto(
                        name = name,
                        unit = unit,
                        quantity = quantity,
                        pantry = "",
                        isPurchased = false
                    )
                )
            ).onFailure { showError("Az új tétel mentése nem sikerült.") }
        }
    }

    /** Meglévő tétel módosítása (név, mennyiség, mértékegység). */
    fun updateItem(itemId: String, name: String, quantity: Double, unit: String) {
        viewModelScope.launch {
            itemRepository.updateItem(itemId, name, quantity, unit)
                .onFailure { showError("A módosítás mentése nem sikerült.") }
        }
    }

    /** Meglévő tétel törlése arról a listáról, amelyikhez tartozik. */
    fun deleteItem(item: ShoppingListItemUi) {
        viewModelScope.launch {
            shoppingListRepository.removeItem(item.listId, item.id)
                .onFailure { showError("A tétel törlése nem sikerült.") }
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    private fun ItemDto.toUi(list: ShoppingListOption) = ShoppingListItemUi(
        id = itemId,
        name = name,
        quantity = quantity,
        unit = unit,
        purchased = isPurchased,
        listId = list.listId,
        tag = list.name
    )
}
