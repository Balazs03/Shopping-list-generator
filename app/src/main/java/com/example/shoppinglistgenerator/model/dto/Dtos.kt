package com.example.shoppinglistgenerator.model.dto
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class UserDto(
    @DocumentId
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val pantries: List<String> = emptyList()
)

data class ItemDto(
    @DocumentId
    val itemId: String = "",
    val name: String = "",
    val unit: String = "",
    val quantity: Double = 0.0,
    @get:PropertyName("purchased")
    @set:PropertyName("purchased")
    var isPurchased: Boolean = false
)

data class RecipeDto(
    @DocumentId
    val recipeId: String = "",
    val name: String = "",
    val description: String = "",
    // https URL vagy base64 "data:image/jpeg;base64,..." URI
    val imageUrl: String = "",
    val prepTimeMinutes: Int = 0,
    val servings: Int = 0,
    // EASY | MEDIUM | HARD
    val difficulty: String = "",
    val ingredients: List<IngredientDto> = emptyList(),
    val instructions: List<String> = emptyList(),
    val items: List<String> = emptyList()
)

// A recept dokumentumba ágyazott hozzávaló (nem külön collection)
data class IngredientDto(
    val name: String = "",
    val quantity: Double = 0.0,
    val unit: String = ""
)

data class PantryDto(
    @DocumentId
    val pantryId: String = "",
    val name: String = "",
    val creator: String = "",
    val password: String = "",
    val recipes: List<String> = emptyList(),
    val items: List<String> = emptyList(),
    val users: List<String> = emptyList()
)

data class ShoppingListDto(
    @DocumentId
    val listId: String = "",
    val pantry: String = "",
    val creator: String = "",
    val items: List<String> = emptyList(),
    val recipes: List<String> = emptyList()
)