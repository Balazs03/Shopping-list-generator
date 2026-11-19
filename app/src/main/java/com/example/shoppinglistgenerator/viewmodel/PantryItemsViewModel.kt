package com.example.shoppinglistgenerator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoppinglistgenerator.model.dto.ItemDto
import com.example.shoppinglistgenerator.model.repository.ItemRepository
import com.example.shoppinglistgenerator.model.repository.PantryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PantryItemsUiState(
    val pantryName: String = "",
    val memberCount: Int = 0,
    val items: List<ItemDto> = emptyList(),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    // A törlés megerősítésére váró tétel (ilyenkor látszik a dialógus)
    val itemPendingDelete: ItemDto? = null,
    val deleteFailed: Boolean = false
)

class PantryItemsViewModel(
    pantryId: String,
    pantryRepository: PantryRepository = PantryRepository(),
    private val itemRepository: ItemRepository = ItemRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PantryItemsUiState())
    val uiState: StateFlow<PantryItemsUiState> = _uiState.asStateFlow()

    init {
        combine(
            pantryRepository.observePantry(pantryId),
            itemRepository.observeItemsByPantry(pantryId)
        ) { pantry, items ->
            _uiState.update {
                it.copy(
                    pantryName = pantry?.name.orEmpty(),
                    memberCount = pantry?.users?.size ?: 0,
                    items = items,
                    isLoading = false,
                    loadFailed = pantry == null
                )
            }
        }
            .catch { _uiState.update { it.copy(isLoading = false, loadFailed = true) } }
            .launchIn(viewModelScope)
    }

    fun onDeleteRequest(item: ItemDto) {
        _uiState.update { it.copy(itemPendingDelete = item) }
    }

    fun onDeleteDismiss() {
        _uiState.update { it.copy(itemPendingDelete = null) }
    }

    fun onDeleteConfirm() {
        val item = _uiState.value.itemPendingDelete ?: return
        _uiState.update { it.copy(itemPendingDelete = null) }
        viewModelScope.launch {
            // A lista a snapshot listener miatt magától frissül sikeres törlés után
            itemRepository.deleteItem(item).onFailure {
                _uiState.update { it.copy(deleteFailed = true) }
            }
        }
    }

    fun onDeleteErrorShown() {
        _uiState.update { it.copy(deleteFailed = false) }
    }
}
