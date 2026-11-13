package com.example.shoppinglistgenerator.viewmodel

import com.example.shoppinglistgenerator.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeViewModelTest {

    private val testRecipes = listOf(
        Recipe(id = "paradicsomos-spagetti", name = "Paradicsomos spagetti"),
        Recipe(id = "tejfolos-krumplileves", name = "Tejfölös krumplileves"),
        Recipe(id = "lecso", name = "Lecsó")
    )

    private lateinit var viewModel: RecipeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = RecipeViewModel(flowOf(testRecipes))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_containsAllRecipes() {
        val state = viewModel.uiState.value

        assertEquals("", state.searchQuery)
        assertFalse(state.isLoading)
        assertEquals(testRecipes, state.recipes)
        assertEquals(testRecipes, viewModel.allRecipes.value)
    }

    @Test
    fun beforeFirstEmission_isLoading() {
        val loadingViewModel = RecipeViewModel(MutableSharedFlow())

        assertTrue(loadingViewModel.uiState.value.isLoading)
    }

    @Test
    fun failedLoad_setsLoadFailed() {
        val failingViewModel = RecipeViewModel(flow { throw IllegalStateException("PERMISSION_DENIED") })

        val state = failingViewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.loadFailed)
    }

    @Test
    fun newEmission_keepsSearchQueryApplied() {
        val source = MutableSharedFlow<List<Recipe>>(extraBufferCapacity = 1)
        val liveViewModel = RecipeViewModel(source)
        liveViewModel.onSearchQueryChange("leves")

        source.tryEmit(testRecipes)

        assertEquals(listOf("Tejfölös krumplileves"), liveViewModel.uiState.value.recipes.map { it.name })
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

        assertEquals(testRecipes, viewModel.uiState.value.recipes)
    }

    @Test
    fun search_doesNotAffectAllRecipes() {
        viewModel.onSearchQueryChange("leves")

        assertEquals(testRecipes, viewModel.allRecipes.value)
    }
}
