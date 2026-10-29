package com.example.shoppinglistgenerator.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RecipeViewModelTest {

    private lateinit var viewModel: RecipeViewModel

    @Before
    fun setUp() {
        viewModel = RecipeViewModel()
    }

    @Test
    fun initialState_containsAllRecipes() {
        val state = viewModel.uiState.value

        assertEquals("", state.searchQuery)
        assertEquals(RecipeViewModel.hardcodedRecipes, state.recipes)
    }

    @Test
    fun search_filtersByName() {
        viewModel.onSearchQueryChange("leves")

        val state = viewModel.uiState.value
        assertEquals("leves", state.searchQuery)
        assertEquals(listOf("Tejfölös krumplileves"), state.recipes.map { it.name })
    }

    @Test
    fun search_isCaseInsensitiveAndTrimmed() {
        viewModel.onSearchQueryChange("  SPAGETTI ")

        assertEquals(listOf("Paradicsomos spagetti"), viewModel.uiState.value.recipes.map { it.name })
    }

    @Test
    fun search_withNoMatch_returnsEmptyList() {
        viewModel.onSearchQueryChange("pizza")

        assertEquals(emptyList<String>(), viewModel.uiState.value.recipes.map { it.name })
    }

    @Test
    fun clearingSearch_restoresAllRecipes() {
        viewModel.onSearchQueryChange("leves")
        viewModel.onSearchQueryChange("")

        assertEquals(RecipeViewModel.hardcodedRecipes, viewModel.uiState.value.recipes)
    }

    @Test
    fun getRecipeById_returnsMatchingRecipe() {
        val recipe = viewModel.getRecipeById("paradicsomos-spagetti")

        assertEquals("Paradicsomos spagetti", recipe?.name)
    }

    @Test
    fun getRecipeById_withUnknownId_returnsNull() {
        assertNull(viewModel.getRecipeById("nem-letezo-recept"))
    }
}
