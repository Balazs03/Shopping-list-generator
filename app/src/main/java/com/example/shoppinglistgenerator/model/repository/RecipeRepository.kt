package com.example.shoppinglistgenerator.model.repository

import com.example.shoppinglistgenerator.model.Difficulty
import com.example.shoppinglistgenerator.model.Ingredient
import com.example.shoppinglistgenerator.model.Recipe
import com.example.shoppinglistgenerator.model.dto.IngredientDto
import com.example.shoppinglistgenerator.model.dto.RecipeDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecipeRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("recipes")

    // Valós idejű receptlista: minden Firestore-változásnál új listát ad
    fun observeRecipes(): Flow<List<Recipe>> =
        collection.snapshots().map { snapshot ->
            snapshot.toObjects<RecipeDto>()
                .filter { it.name.isNotBlank() } // üres teszt dokumentumok kiszűrése
                .map { it.toRecipe() }
        }
}

private fun RecipeDto.toRecipe() = Recipe(
    id = recipeId,
    name = name,
    description = description,
    imageUrl = imageUrl,
    prepTimeMinutes = prepTimeMinutes,
    servings = servings,
    difficulty = Difficulty.entries.find { it.name == difficulty } ?: Difficulty.EASY,
    ingredients = ingredients.map { it.toIngredient() },
    steps = instructions
)

private fun IngredientDto.toIngredient() = Ingredient(
    name = name,
    quantity = quantity,
    unit = unit
)
