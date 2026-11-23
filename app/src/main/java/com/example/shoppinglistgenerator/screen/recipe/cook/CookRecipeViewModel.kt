package com.example.shoppinglistgenerator.screen.recipe.cook

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoppinglistgenerator.R
import com.example.shoppinglistgenerator.model.Ingredient
import com.example.shoppinglistgenerator.model.dto.ItemDto
import com.example.shoppinglistgenerator.model.dto.PantryDto
import com.example.shoppinglistgenerator.model.repository.AuthRepository
import com.example.shoppinglistgenerator.model.repository.ItemRepository
import com.example.shoppinglistgenerator.model.repository.PantryRepository
import com.example.shoppinglistgenerator.model.repository.ShoppingListOption
import com.example.shoppinglistgenerator.model.repository.ShoppingListRepository
import com.example.shoppinglistgenerator.model.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "CookRecipe"

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
    val messageArgs: List<Any> = emptyList()
)

class CookRecipeViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val pantryRepository: PantryRepository = PantryRepository(),
    private val itemRepository: ItemRepository = ItemRepository(),
    private val shoppingListRepository: ShoppingListRepository = ShoppingListRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CookRecipeUiState())
    val uiState: StateFlow<CookRecipeUiState> = _uiState.asStateFlow()

    private var recipeId: String = ""
    private var ingredients: List<Ingredient> = emptyList()

    fun onCookClick(recipeId: String, recipeIngredients: List<Ingredient>) {
        if (_uiState.value.step != CookStep.Idle) return
        this.recipeId = recipeId
        ingredients = recipeIngredients
        _uiState.update { it.copy(step = CookStep.Loading) }

        viewModelScope.launch {
            val pantries = runCatching {
                val uid = authRepository.ensureAuthenticated()
                val pantryIds = userRepository.getUser(uid).getOrThrow()?.pantries.orEmpty()
                pantryRepository.getPantriesByIds(pantryIds).getOrThrow()
            }.getOrElse { error ->
                Log.e(TAG, "Kamrák betöltése sikertelen", error)
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
            val plan = step.plan
            val pantry = step.pantry

            // 1. Levonás a kamrából
            val quantities = plan.deductions.associate { it.item.itemId to it.newQuantity }
            itemRepository.updateQuantities(quantities).onFailure { error ->
                Log.e(TAG, "Mennyiségek mentése sikertelen", error)
                showMessage(R.string.cook_error_generic)
                return@launch
            }

            if (plan.missing.isEmpty()) {
                showMessage(R.string.cook_success, pantry.name)
                return@launch
            }

            // 2. A hiányzók a kiválasztott kamra bevásárlólistájára
            runCatching {
                val uid = authRepository.ensureAuthenticated()
                shoppingListRepository.addItemsToList(
                    list = pantry.shoppingList(uid),
                    userId = uid,
                    newItems = plan.missing.map {
                        ItemDto(name = it.name, unit = it.unit, quantity = roundAmount(it.quantity))
                    },
                    recipeIds = listOf(recipeId).filter { it.isNotBlank() }
                ).getOrThrow()
            }.onSuccess {
                showMessage(R.string.cook_success_with_list, pantry.name, plan.missing.size)
            }.onFailure { error ->
                Log.e(TAG, "Hiányzók listára tétele sikertelen", error)
                // A levonás már megtörtént, ezt külön jelezzük
                showMessage(R.string.cook_error_list, pantry.name)
            }
        }
    }

    fun onDismiss() {
        // Mentés közben nem szakítjuk meg a folyamatot
        if (_uiState.value.step is CookStep.Saving) return
        _uiState.update { it.copy(step = CookStep.Idle) }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(messageRes = null, messageArgs = emptyList()) }
    }

    private suspend fun preparePlan(pantry: PantryDto) {
        itemRepository.getItemsByPantry(pantry.pantryId)
            .onSuccess { items ->
                val plan = planCooking(ingredients, items)
                _uiState.update { it.copy(step = CookStep.Confirm(pantry, plan)) }
            }
            .onFailure { error ->
                Log.e(TAG, "Kamra alapanyagainak lekérése sikertelen", error)
                showMessage(R.string.cook_error_generic)
            }
    }

    private fun showMessage(@StringRes res: Int, vararg args: Any) {
        _uiState.update { it.copy(step = CookStep.Idle, messageRes = res, messageArgs = args.toList()) }
    }

    // Kerekítés, hogy pl. 0.33333 helyett 0.33 kerüljön a listára
    private fun roundAmount(value: Double) = Math.round(value * 100) / 100.0
}

/** Saját kamra: az alapértelmezett személyes kamra, vagy aminek csak egy tagja van. */
internal fun PantryDto.isPersonalPantry() = isDefaultPersonal || users.size <= 1

// Az alapértelmezett saját kamránál a felhasználó saját listája, máskor a kamra közös listája
private fun PantryDto.shoppingList(uid: String): ShoppingListOption =
    if (isDefaultPersonal) ShoppingListOption.personal(uid)
    else ShoppingListOption.pantry(pantryId, name)
