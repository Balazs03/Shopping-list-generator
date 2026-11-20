package com.example.shoppinglistgenerator.screen.createpantry

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CreatePantryUiState(
    val pantryName: String = "",
    val joinCode: String = "",
    val isLoading: Boolean = false,
    val isCreated: Boolean = false,
    val errorMessage: String? = null
)

class CreatePantryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePantryUiState())
    val uiState: StateFlow<CreatePantryUiState> = _uiState.asStateFlow()

    fun onPantryNameChange(newName: String) {
        _uiState.update { it.copy(pantryName = newName, errorMessage = null) }
    }

    fun onJoinCodeChange(newCode: String) {
        _uiState.update { it.copy(joinCode = newCode, errorMessage = null) }
    }

    fun createSharedPantry(currentPantryCount: Int) {
        val state = _uiState.value

        // Üzleti szabály ellenőrzése: max 1 saját + max 1 közös kamra (összesen max 2)
        if (currentPantryCount >= 2) {
            _uiState.update {
                it.copy(errorMessage = "Már van közös kamrád. Új létrehozásához előbb töröld a meglévőt vagy lépj ki belőle.")
            }
            return
        }

        if (state.pantryName.isBlank() || state.joinCode.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "A kamra nevének és a jelkódnak a megadása kötelező.")
            }
            return
        }

        // TODO: Később ide kerül a repository.createPantry(state.pantryName.trim(), state.joinCode.trim()) hívás
        _uiState.update {
            it.copy(isCreated = true, errorMessage = null)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}