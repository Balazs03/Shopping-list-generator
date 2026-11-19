package com.example.shoppinglistgenerator.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoppinglistgenerator.R
import com.example.shoppinglistgenerator.model.dto.PantryDto
import com.example.shoppinglistgenerator.model.repository.AuthRepository
import com.example.shoppinglistgenerator.model.repository.PantryJoinException
import com.example.shoppinglistgenerator.model.repository.PantryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PantryJoinUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    @StringRes val errorRes: Int? = null,
    // Sikeres csatlakozás után a kamra, amihez csatlakozott
    val joinedPantry: PantryDto? = null
)

class PantryJoinViewModel(
    private val pantryRepository: PantryRepository = PantryRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PantryJoinUiState())
    val uiState: StateFlow<PantryJoinUiState> = _uiState.asStateFlow()

    fun onCodeChange(code: String) {
        _uiState.update { it.copy(code = code.trim(), errorRes = null) }
    }

    fun joinPantry() {
        val code = _uiState.value.code
        if (code.isBlank()) {
            _uiState.update { it.copy(errorRes = R.string.pantry_join_error_empty_code) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorRes = null) }
            val result = runCatching { authRepository.ensureAuthenticated() }
                .mapCatching { uid -> pantryRepository.joinPantry(code, uid).getOrThrow() }

            result.onSuccess { pantry ->
                _uiState.update { it.copy(isLoading = false, joinedPantry = pantry) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorRes = error.toMessageRes()) }
            }
        }
    }

    private fun Throwable.toMessageRes(): Int = when (this) {
        is PantryJoinException.NotFound -> R.string.pantry_join_error_not_found
        is PantryJoinException.AlreadyMember -> R.string.pantry_join_error_already_member
        is PantryJoinException.LimitReached -> R.string.pantry_join_error_limit
        else -> R.string.pantry_join_error_generic
    }
}
