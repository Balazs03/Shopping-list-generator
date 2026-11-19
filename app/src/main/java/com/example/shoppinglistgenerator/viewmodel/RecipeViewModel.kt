package com.example.shoppinglistgenerator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoppinglistgenerator.model.Recipe
import com.example.shoppinglistgenerator.model.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class RecipeListUiState(
    val searchQuery: String = "",
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false
)

class RecipeViewModel(
    recipes: Flow<List<Recipe>> = RecipeRepository().observeRecipes()
) : ViewModel() {

    private val _allRecipes = MutableStateFlow<List<Recipe>>(emptyList())

    // Every loaded recipe regardless of the search query, used by the detail screen
    val allRecipes: StateFlow<List<Recipe>> = _allRecipes.asStateFlow()

    private val _uiState = MutableStateFlow(RecipeListUiState())
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()

    init {
        recipes
            .onEach { loadedRecipes ->
                _allRecipes.value = loadedRecipes
                _uiState.update { state ->
                    state.copy(
                        recipes = loadedRecipes.filterByName(state.searchQuery),
                        isLoading = false,
                        loadFailed = false
                    )
                }
            }
            .catch { _uiState.update { it.copy(isLoading = false, loadFailed = true) } }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                recipes = _allRecipes.value.filterByName(query)
            )
        }
    }

    private fun List<Recipe>.filterByName(query: String): List<Recipe> {
        val trimmedQuery = query.trim()
        return filter { it.name.contains(trimmedQuery, ignoreCase = true) }
    }
}
