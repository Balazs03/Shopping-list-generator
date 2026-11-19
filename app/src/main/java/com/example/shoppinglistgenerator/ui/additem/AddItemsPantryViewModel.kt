package com.example.shoppinglistgenerator.ui.additem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoppinglistgenerator.model.dto.ItemDto
import com.example.shoppinglistgenerator.model.repository.ItemRepository
import com.example.shoppinglistgenerator.model.repository.PantryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PantryOption(
    val id: String,
    val name: String,
    val isShared: Boolean = false
)

data class AddItemsPantryUiState(
    val name: String = "",
    val quantity: String = "1",
    val unit: String = "l",
    val selectedPantryId: String = "otthoni",
    val selectedPantryName: String = "Otthoni",
    val availablePantries: List<PantryOption> = listOf(
        PantryOption(id = "sajat", name = "Saját", isShared = false),
        PantryOption(id = "otthoni", name = "Otthoni", isShared = true)
    ),
    val availableUnits: List<String> = listOf("l", "dl", "ml", "g", "kg", "db", "csomag", "üveg", "doboz"),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class AddItemsPantryViewModel(
    private val itemRepository: ItemRepository = ItemRepository(),
    private val pantryRepository: PantryRepository = PantryRepository(),
    currentPantryId: String = "otthoni"
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AddItemsPantryUiState(
            selectedPantryId = currentPantryId,
            selectedPantryName = if (currentPantryId == "sajat") "Saját" else "Otthoni"
        )
    )
    val uiState: StateFlow<AddItemsPantryUiState> = _uiState.asStateFlow()

    fun updateCurrentPantry(pantryId: String) {
        if (pantryId.isNotBlank()) {
            val option = _uiState.value.availablePantries.find { it.id == pantryId }
            val pantryName = option?.name ?: pantryId
            _uiState.update {
                it.copy(
                    selectedPantryId = pantryId,
                    selectedPantryName = pantryName
                )
            }
        }
    }

    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(name = newName, errorMessage = null) }
    }

    fun onQuantityChanged(newQuantity: String) {
        if (newQuantity.isEmpty() || newQuantity.matches(Regex("^\\d*([.,]\\d*)?$"))) {
            _uiState.update { it.copy(quantity = newQuantity, errorMessage = null) }
        }
    }

    fun onUnitSelected(newUnit: String) {
        _uiState.update { it.copy(unit = newUnit) }
    }

    fun onPantrySelected(pantry: PantryOption) {
        _uiState.update {
            it.copy(
                selectedPantryId = pantry.id,
                selectedPantryName = pantry.name
            )
        }
    }

    fun createItem(onSuccess: () -> Unit = {}) {
        val currentState = _uiState.value
        val nameTrimmed = currentState.name.trim()

        if (nameTrimmed.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Kérjük, add meg az alapanyag nevét!") }
            return
        }

        val qtyDouble = currentState.quantity.replace(',', '.').toDoubleOrNull() ?: 1.0

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val newItem = ItemDto(
                name = nameTrimmed,
                quantity = qtyDouble,
                unit = currentState.unit,
                pantry = currentState.selectedPantryId,
                isPurchased = false
            )

            val result = itemRepository.createItem(newItem)

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                onSuccess()
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage ?: "Hiba történt az elem mentése során."
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}