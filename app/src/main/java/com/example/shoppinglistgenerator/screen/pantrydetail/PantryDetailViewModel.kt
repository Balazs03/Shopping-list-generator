package com.example.shoppinglistgenerator.screen.pantrydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoppinglistgenerator.model.repository.AuthRepository
import com.example.shoppinglistgenerator.model.repository.ItemRepository
import com.example.shoppinglistgenerator.model.repository.PantryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

class PantryDetailViewModel(
    private val pantryRepository: PantryRepository = PantryRepository(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val itemRepository: ItemRepository = ItemRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PantryDetailUiState(isLoading = true))
    val uiState: StateFlow<PantryDetailUiState> = _uiState.asStateFlow()

    init {
        loadPantries()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadPantries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val uid = runCatching { authRepository.ensureAuthenticated() }.getOrNull()
            if (uid == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Nem sikerült a bejelentkezés.") }
                return@launch
            }

            pantryRepository.observePantriesForUser(uid)
                .flatMapLatest { pantries ->
                    if (pantries.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        val flows = pantries.map { pantry ->
                            itemRepository.observeItemsByPantry(pantry.pantryId).map { items ->
                                PantryUiItem(
                                    id = pantry.pantryId,
                                    name = pantry.name,
                                    ingredientCount = items.size,
                                    memberCount = pantry.users.size,
                                    isOwner = pantry.creator == uid || pantry.isDefaultPersonal,
                                    isDefaultPersonal = pantry.isDefaultPersonal
                                )
                            }
                        }
                        combine(flows) { array -> array.toList() }
                    }
                }
                .catch { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Hiba a kamrák betöltésekor.") }
                }
                .collect { uiItems ->
                    _uiState.update {
                        it.copy(
                            pantries = uiItems,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun deleteSharedPantry(pantryId: String) {
        val target = _uiState.value.pantries.find { it.id == pantryId } ?: return
        if (target.isDefaultPersonal) return // A default saját kamra nem törölhető

        viewModelScope.launch {
            val uid = runCatching { authRepository.ensureAuthenticated() }.getOrNull() ?: return@launch
            pantryRepository.deleteSharedPantry(pantryId, uid).onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "A kamra törlése nem sikerült.") }
            }
        }
    }

    fun leaveSharedPantry(pantryId: String) {
        val target = _uiState.value.pantries.find { it.id == pantryId } ?: return
        if (target.isDefaultPersonal) return

        viewModelScope.launch {
            val uid = runCatching { authRepository.ensureAuthenticated() }.getOrNull() ?: return@launch
            pantryRepository.leaveSharedPantry(pantryId, uid).onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "A kamrából való kilépés nem sikerült.") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}