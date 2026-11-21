package com.example.shoppinglistgenerator.screen.createpantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoppinglistgenerator.model.repository.AuthRepository
import com.example.shoppinglistgenerator.model.repository.PantryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreatePantryUiState(
    val pantryName: String = "",
    val joinCode: String = "",
    val isLoading: Boolean = false,
    val isCreated: Boolean = false,
    val errorMessage: String? = null
)

class CreatePantryViewModel(
    private val pantryRepository: PantryRepository = PantryRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePantryUiState())
    val uiState: StateFlow<CreatePantryUiState> = _uiState.asStateFlow()

    fun onPantryNameChange(newName: String) {
        _uiState.update { it.copy(pantryName = newName, errorMessage = null) }
    }

    fun onJoinCodeChange(newCode: String) {
        _uiState.update { it.copy(joinCode = newCode, errorMessage = null) }
    }

    fun createSharedPantry(currentPantryCount: Int = 1) {
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

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = runCatching { authRepository.ensureAuthenticated() }
                .mapCatching { uid ->
                    pantryRepository.createPantry(
                        name = state.pantryName.trim(),
                        joinCode = state.joinCode.trim(),
                        uid = uid
                    ).getOrThrow()
                }

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isCreated = true) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Hiba történt a kamra létrehozásakor."
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}