package com.example.shoppinglistgenerator.model

// Default values are required for Firestore's toObject() deserialization
data class Ingredient(
    val name: String = "",
    val quantity: Double = 0.0,
    val unit: String = ""
)
