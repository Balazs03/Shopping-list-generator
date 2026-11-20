package com.example.shoppinglistgenerator.ui.pantrydetail

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PantryDetailUiState(
    val pantries: List<PantryUiItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    // 1. hely: alapértelmezett saját kamra (mindenkinek van default)
    // 2. hely: opcionális közös kamra (max. 1 db)
    val hasSharedPantry: Boolean get() = pantries.size >= 2
    val canAddOrJoinSharedPantry: Boolean get() = pantries.size < 2
}

class PantryDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        // Egyelőre beégetett tesztadatok; később innen hívjuk a model.repository-t
        PantryDetailUiState(
            pantries = listOf(
                PantryUiItem("1", "Saját kamra", 5, 1, isOwner = true, isDefaultPersonal = true),
                PantryUiItem("2", "Otthoni kamra", 8, 3, isOwner = false, isDefaultPersonal = false)
            )
        )
    )
    val uiState: StateFlow<PantryDetailUiState> = _uiState.asStateFlow()

    fun deleteSharedPantry(pantryId: String) {
        val target = _uiState.value.pantries.find { it.id == pantryId } ?: return
        if (target.isDefaultPersonal) return // A default saját kamra nem törölhető

        // TODO: Később ide kerül a repository.deletePantry(pantryId) hívás
        _uiState.update { state ->
            state.copy(pantries = state.pantries.filterNot { it.id == pantryId })
        }
    }

    fun leaveSharedPantry(pantryId: String) {
        val target = _uiState.value.pantries.find { it.id == pantryId } ?: return
        if (target.isDefaultPersonal) return

        // TODO: Később ide kerül a repository.leavePantry(pantryId) hívás
        _uiState.update { state ->
            state.copy(pantries = state.pantries.filterNot { it.id == pantryId })
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}