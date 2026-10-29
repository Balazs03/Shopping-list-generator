package com.example.shoppinglistgenerator.viewmodel

import androidx.lifecycle.ViewModel
import com.example.shoppinglistgenerator.model.Difficulty
import com.example.shoppinglistgenerator.model.Ingredient
import com.example.shoppinglistgenerator.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RecipeListUiState(
    val searchQuery: String = "",
    val recipes: List<Recipe> = emptyList()
)

class RecipeViewModel : ViewModel() {

    // TODO: load recipes from Firestore instead of the hardcoded list
    private val allRecipes: List<Recipe> = hardcodedRecipes

    private val _uiState = MutableStateFlow(RecipeListUiState(recipes = allRecipes))
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        val trimmedQuery = query.trim()
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                recipes = allRecipes.filter { it.name.contains(trimmedQuery, ignoreCase = true) }
            )
        }
    }

    fun getRecipeById(id: String): Recipe? = allRecipes.find { it.id == id }

    companion object {
        val hardcodedRecipes = listOf(
            Recipe(
                id = "paradicsomos-spagetti",
                name = "Paradicsomos spagetti",
                description = "Bazsalikommal és parmezánnal",
                prepTimeMinutes = 25,
                servings = 4,
                difficulty = Difficulty.EASY,
                ingredients = listOf(
                    Ingredient("Spagetti", 400.0, "g"),
                    Ingredient("Darabolt paradicsom", 400.0, "g"),
                    Ingredient("Fokhagyma", 1.0, "db"),
                    Ingredient("Olívaolaj", 30.0, "ml"),
                    Ingredient("Bazsalikom", 10.0, "g"),
                    Ingredient("Parmezán", 60.0, "g")
                ),
                steps = listOf(
                    "Főzd a spagettit sós vízben 9–11 percig.",
                    "Az olajon futtasd meg a fokhagymát, add hozzá a paradicsomot, és főzd 10 percig.",
                    "Forgasd bele a tésztát és a bazsalikomot. Parmezánnal tálald."
                )
            ),
            Recipe(
                id = "tejfolos-krumplileves",
                name = "Tejfölös krumplileves",
                description = "Egyszerű, melengető klasszikus",
                prepTimeMinutes = 40,
                servings = 4,
                difficulty = Difficulty.EASY,
                ingredients = listOf(
                    Ingredient("Burgonya", 800.0, "g"),
                    Ingredient("Vöröshagyma", 1.0, "db"),
                    Ingredient("Tejföl", 200.0, "g"),
                    Ingredient("Liszt", 20.0, "g"),
                    Ingredient("Pirospaprika", 5.0, "g"),
                    Ingredient("Babérlevél", 2.0, "db"),
                    Ingredient("Napraforgóolaj", 20.0, "ml")
                ),
                steps = listOf(
                    "A hagymát apróra vágva az olajon dinszteld üvegesre.",
                    "Szórd meg a pirospaprikával, add hozzá a kockára vágott burgonyát és a babérlevelet, majd öntsd fel 1,5 liter vízzel.",
                    "Sózd, és főzd 20–25 percig, amíg a burgonya megpuhul.",
                    "A tejfölt keverd simára a liszttel, habard be vele a levest, és forrald még 3 percig."
                )
            ),
            Recipe(
                id = "zoldseges-rizotto",
                name = "Zöldséges rizottó",
                description = "Krémes rizs, szezonális zöldségek",
                prepTimeMinutes = 35,
                servings = 4,
                difficulty = Difficulty.MEDIUM,
                ingredients = listOf(
                    Ingredient("Arborio rizs", 300.0, "g"),
                    Ingredient("Zöldségalaplé", 1.0, "l"),
                    Ingredient("Cukkini", 1.0, "db"),
                    Ingredient("Sárgarépa", 2.0, "db"),
                    Ingredient("Zöldborsó", 150.0, "g"),
                    Ingredient("Vöröshagyma", 1.0, "db"),
                    Ingredient("Vaj", 30.0, "g"),
                    Ingredient("Parmezán", 50.0, "g")
                ),
                steps = listOf(
                    "A hagymát a vaj felén párold üvegesre, add hozzá a rizst, és kevergetve pirítsd 2 percig.",
                    "Merőkanalanként öntsd hozzá a forró alaplét, és folyamatos kevergetés mellett főzd 18–20 percig.",
                    "Közben add hozzá a felkockázott zöldségeket és a zöldborsót.",
                    "A tűzről levéve keverd bele a maradék vajat és a parmezánt."
                )
            ),
            Recipe(
                id = "tejfolos-gombaporkolt",
                name = "Tejfölös gombapörkölt",
                description = "Egyszerű, gyorsan elkészülő étel",
                prepTimeMinutes = 35,
                servings = 4,
                difficulty = Difficulty.EASY,
                ingredients = listOf(
                    Ingredient("Csiperkegomba", 600.0, "g"),
                    Ingredient("Vöröshagyma", 1.0, "db"),
                    Ingredient("Tejföl", 200.0, "g"),
                    Ingredient("Pirospaprika", 10.0, "g"),
                    Ingredient("Petrezselyem", 10.0, "g"),
                    Ingredient("Napraforgóolaj", 30.0, "ml")
                ),
                steps = listOf(
                    "A hagymát apróra vágva az olajon párold üvegesre.",
                    "Szórd meg a pirospaprikával, add hozzá a felszeletelt gombát, sózd, és fedő alatt párold 15 percig.",
                    "Keverd bele a tejfölt, forrald össze, végül szórd meg petrezselyemmel."
                )
            ),
            Recipe(
                id = "zoldborsofozelek",
                name = "Zöldborsófőzelék",
                description = "Egyszerű, gyors és klasszikus",
                prepTimeMinutes = 25,
                servings = 4,
                difficulty = Difficulty.EASY,
                ingredients = listOf(
                    Ingredient("Zöldborsó", 500.0, "g"),
                    Ingredient("Vaj", 30.0, "g"),
                    Ingredient("Liszt", 30.0, "g"),
                    Ingredient("Tej", 300.0, "ml"),
                    Ingredient("Cukor", 10.0, "g"),
                    Ingredient("Petrezselyem", 10.0, "g")
                ),
                steps = listOf(
                    "A vajon pirítsd világosra a lisztet.",
                    "Add hozzá a zöldborsót, öntsd fel a tejjel és kevés vízzel, majd sózd és cukrozd.",
                    "Főzd 10 percig, amíg besűrűsödik, végül keverd bele a petrezselymet."
                )
            )
        )
    }
}
