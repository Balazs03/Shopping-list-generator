package com.example.shoppinglistgenerator.screen.recipe.cook

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoppinglistgenerator.R
import com.example.shoppinglistgenerator.model.Ingredient
import com.example.shoppinglistgenerator.model.dto.PantryDto
import com.example.shoppinglistgenerator.model.repository.AuthRepository
import com.example.shoppinglistgenerator.model.repository.ItemRepository
import com.example.shoppinglistgenerator.model.repository.PantryRepository
import com.example.shoppinglistgenerator.model.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Az "Elkészítem" folyamat lépései (mindegyikhez más dialógus tartozik). */
sealed interface CookStep {
    data object Idle : CookStep
    data object Loading : CookStep

    /** Több kamrája van a felhasználónak: választania kell (saját / közös). */
    data class ChoosePantry(val pantries: List<PantryDto>) : CookStep

    /** A kiválasztott kamrából ezt vonnánk le; a felhasználó megerősíti. */
    data class Confirm(val pantry: PantryDto, val plan: CookPlan) : CookStep
    data class Saving(val pantry: PantryDto, val plan: CookPlan) : CookStep
}

data class CookRecipeUiState(
    val step: CookStep = CookStep.Idle,
    // Egyszeri üzenet a snackbar-nak (siker vagy hiba), megjelenítés után törölni kell
    @StringRes val messageRes: Int? = null,
    val messageArg: String = ""
)

class CookRecipeViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val pantryRepository: PantryRepository = PantryRepository(),
    private val itemRepository: ItemRepository = ItemRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CookRecipeUiState())
    val uiState: StateFlow<CookRecipeUiState> = _uiState.asStateFlow()

    private var ingredients: List<Ingredient> = emptyList()

    fun onCookClick(recipeIngredients: List<Ingredient>) {
        if (_uiState.value.step != CookStep.Idle) return
        ingredients = recipeIngredients
        _uiState.update { it.copy(step = CookStep.Loading) }

        viewModelScope.launch {
            val pantries = runCatching {
                val uid = authRepository.ensureAuthenticated()
                val pantryIds = userRepository.getUser(uid).getOrThrow()?.pantries.orEmpty()
                pantryRepository.getPantriesByIds(pantryIds).getOrThrow()
            }.getOrElse {
                showMessage(R.string.cook_error_generic)
                return@launch
            }

            when (pantries.size) {
                0 -> showMessage(R.string.cook_error_no_pantry)
                1 -> preparePlan(pantries.single())
                else -> _uiState.update { it.copy(step = CookStep.ChoosePantry(pantries)) }
            }
        }
    }

    fun onPantryChosen(pantry: PantryDto) {
        _uiState.update { it.copy(step = CookStep.Loading) }
        viewModelScope.launch { preparePlan(pantry) }
    }

    fun onConfirm() {
        val step = _uiState.value.step as? CookStep.Confirm ?: return
        _uiState.update { it.copy(step = CookStep.Saving(step.pantry, step.plan)) }

        viewModelScope.launch {
            val quantities = step.plan.deductions.associate { it.item.itemId to it.newQuantity }
            itemRepository.updateQuantities(quantities)
                .onSuccess { showMessage(R.string.cook_success, step.pantry.name) }
                .onFailure { showMessage(R.string.cook_error_generic) }
        }
    }

    fun onDismiss() {
        // Mentés közben nem szakítjuk meg a folyamatot
        if (_uiState.value.step is CookStep.Saving) return
        _uiState.update { it.copy(step = CookStep.Idle) }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(messageRes = null, messageArg = "") }
    }

    private suspend fun preparePlan(pantry: PantryDto) {
        itemRepository.getItemsByPantry(pantry.pantryId)
            .onSuccess { items ->
                val plan = planCooking(ingredients, items)
                _uiState.update { it.copy(step = CookStep.Confirm(pantry, plan)) }
            }
            .onFailure { showMessage(R.string.cook_error_generic) }
    }

    private fun showMessage(@StringRes res: Int, arg: String = "") {
        _uiState.update { it.copy(step = CookStep.Idle, messageRes = res, messageArg = arg) }
    }
}
