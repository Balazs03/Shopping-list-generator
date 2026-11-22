package com.example.shoppinglistgenerator.navigation

import kotlinx.serialization.Serializable

/**
/ Type-safe navigation destinations for Shopping List Generator app.
 */
sealed interface Screen {

    /**
     * Kamra létrehozása képernyő (Név és jelkód kötelező).
     */
    @Serializable
    data object CreatePantry : Screen

    /**
     * Kamra részletei képernyő (Tulajdonságok, funkciók, törlés - owner, kilépés - non-owner).
     */
    @Serializable
    data class PantryDetail(val pantryId: String = "") : Screen

    /**
     * Csatlakozás meglévő kamrához (jelkód megadásával).
     */
    @Serializable
    data object PantryJoin : Screen

    /**
     * Kamra elemeinek kilistázása és kezelése.
     */
    @Serializable
    data class PantryItems(val pantryId: String) : Screen

    /**
     * Elem hozzáadása kamrához (Név, mennyiség, mértékegység, Adott kamrához/saját, Tulajdonos).
     */
    @Serializable
    data class AddItemsPantry(val pantryId: String = "otthoni") : Screen

    /**
     * A "Lista" fül: a felhasználó egyetlen bevásárlólistája
     * (Tickerbox, szűrés, új elem, törlés). A név a bottom navigation miatt maradt.
     */
    @Serializable
    data class CreateShoppingList(val pantryId: String? = null) : Screen

    /**
     * Receptek hiányzó hozzávalóinak listára tétele.
     * [targetPantryId] = null: a saját listára; különben az adott kamra közös listájára.
     */
    @Serializable
    data class AddRecipesToShoppingList(val targetPantryId: String? = null) : Screen

    /**
     * Receptek listázása.
     */
    @Serializable
    data object RecipeList : Screen

    /**
     * Recept részletei (Leírás, elkészítés gomb kamracsoporttal/egyénileg).
     */
    @Serializable
    data class RecipeDetail(val recipeId: String) : Screen
}