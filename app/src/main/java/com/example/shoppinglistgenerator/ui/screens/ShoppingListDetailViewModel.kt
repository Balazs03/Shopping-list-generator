package com.example.shoppinglistgenerator.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.shoppinglistgenerator.model.dto.ItemDto
import com.example.shoppinglistgenerator.model.dto.ShoppingListDto
import com.example.shoppinglistgenerator.model.repository.ItemRepository
import com.example.shoppinglistgenerator.model.repository.ShoppingListRepository
import com.example.shoppinglistgenerator.navigation.Screen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Egy tétel a listanézetben. */
data class ShoppingListItemUi(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val purchased: Boolean
)

data class ShoppingListDetailUiState(
    val isLoading: Boolean = true,
    val listNotFound: Boolean = false,
    val items: List<ShoppingListItemUi> = emptyList(),
    /** false = "Összes", true = "Hiányzó" (még nem kipipált) tételek. */
    val showOnlyRemaining: Boolean = false,
    val errorMessage: String? = null
) {
    val visibleItems: List<ShoppingListItemUi>
        get() = if (showOnlyRemaining) items.filter { !it.purchased } else items

    val purchasedCount: Int
        get() = items.count { it.purchased }

    val progress: Float
        get() = if (items.isEmpty()) 0f else purchasedCount.toFloat() / items.size
}

class ShoppingListDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val listId = savedStateHandle.toRoute<Screen.ShoppingListDetail>().listId

    private val shoppingListRepository = ShoppingListRepository()
    private val itemRepository = ItemRepository()

    private val _uiState = MutableStateFlow(ShoppingListDetailUiState())
    val uiState: StateFlow<ShoppingListDetailUiState> = _uiState.asStateFlow()

    init {
        observeList()
    }

    /**
     * Valós idejű figyelés: a lista és a tételei is Firestore snapshot listenerrel érkeznek.
     * Offline módban a helyi cache-ből jön az utoljára betöltött állapot, így a lista látható marad.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeList() {
        viewModelScope.launch {
            shoppingListRepository.observeList(listId)
                .flatMapLatest<ShoppingListDto?, List<ItemDto>?> { list ->
                    if (list == null) flowOf(null)
                    else itemRepository.observeItemsByIds(list.items)
                }
                .catch {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "A lista betöltése nem sikerült.")
                    }
                }
                .collect { items ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            listNotFound = items == null,
                            // A kipipált tételek a lista aljára kerülnek, a Figma szerint.
                            items = items.orEmpty()
                                .map { dto -> dto.toUi() }
                                .sortedWith(
                                    compareBy<ShoppingListItemUi>({ ui -> ui.purchased }, { ui -> ui.name.lowercase() })
                                )
                        )
                    }
                }
        }
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

    /** Új tétel felvétele a listára. */
    fun addItem(name: String, quantity: Double, unit: String) {
        viewModelScope.launch {
            shoppingListRepository.addItem(
                listId = listId,
                item = ItemDto(
                    name = name,
                    unit = unit,
                    quantity = quantity,
                    pantry = "",
                    isPurchased = false
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

    /** Meglévő tétel törlése a listáról. */
    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            shoppingListRepository.removeItem(listId, itemId)
                .onFailure { showError("A tétel törlése nem sikerült.") }
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    private fun ItemDto.toUi() = ShoppingListItemUi(
        id = itemId,
        name = name,
        quantity = quantity,
        unit = unit,
        purchased = isPurchased
    )
}