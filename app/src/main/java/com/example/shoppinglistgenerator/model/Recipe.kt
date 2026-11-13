package com.example.shoppinglistgenerator.model

// Default values are required for Firestore's toObject() deserialization
data class Recipe(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val prepTimeMinutes: Int = 0,
    val servings: Int = 0,
    val difficulty: Difficulty = Difficulty.EASY,
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<String> = emptyList()
)
