package com.example.shoppinglistgenerator.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.shoppinglistgenerator.screen.additemtopantry.AddItemsPantryView
import com.example.shoppinglistgenerator.screen.createshoppinglist.CreateShoppingListScreen
import com.example.shoppinglistgenerator.screen.shoppinglistdetail.ShoppingListDetailScreen
import com.example.shoppinglistgenerator.screen.createpantry.CreatePantryScreen
import com.example.shoppinglistgenerator.screen.pantrydetail.PantryDetailScreen
import com.example.shoppinglistgenerator.screen.pantryitems.PantryItemsScreen
import com.example.shoppinglistgenerator.screen.pantryjoin.PantryJoinScreen
import com.example.shoppinglistgenerator.screen.recipe.RecipeDetailScreen
import com.example.shoppinglistgenerator.screen.recipe.RecipeListScreen
import com.example.shoppinglistgenerator.screen.recipe.RecipeViewModel
import com.example.shoppinglistgenerator.screen.recipe.cook.CookRecipeOverlay
import com.example.shoppinglistgenerator.screen.recipe.cook.CookRecipeViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Screen = Screen.PantryDetail()
) {
    // Activity-scoped, shared by the recipe list and the recipe detail screen
    val recipeViewModel: RecipeViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 1. Kamra elemei (a PantryDetail "Kamra megnyitása" gombjáról)
        composable<Screen.PantryItems> { backStackEntry ->
            val route: Screen.PantryItems = backStackEntry.toRoute()
            PantryItemsScreen(
                pantryId = route.pantryId,
                onBackClick = { navController.popBackStack() },
                onAddItemClick = {
                    navController.navigate(Screen.AddItemsPantry(pantryId = route.pantryId))
                },
                onEditItemClick = { itemId ->
                    // TODO: Navigate to Edit Item Screen once available
                }
            )
        }

        // 2. Kamra létrehozása
        composable<Screen.CreatePantry> {
            CreatePantryScreen(
                onBackClick = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.PantryDetail())
                    }
                },
                onCreated = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.PantryDetail())
                    }
                }
            )
        }

        // 3. Kamráim (Kezdőképernyő): a felhasználó kamráinak listája
        composable<Screen.PantryDetail> {
            PantryDetailScreen(
                onOpenPantry = { pantryId ->
                    navController.navigate(Screen.PantryItems(pantryId = pantryId))
                },
                onDeletePantry = { pantryId ->
                    // Firestore snapshot listener automatikusan frissíti a listát
                },
                onLeavePantry = { pantryId ->
                    // Firestore snapshot listener automatikusan frissíti a listát
                },
                onAddPantryClick = {
                    navController.navigate(Screen.CreatePantry)
                },
                onJoinPantryClick = {
                    navController.navigate(Screen.PantryJoin)
                }
            )
        }

        // 4. Meglévő kamrához csatlakozás (jelkóddal)
        composable<Screen.PantryJoin> {
            PantryJoinScreen(
                // Vissza a kamrák listájára (PantryDetail)
                onBackClick = { navController.popBackStack() },
                onJoined = { joinedPantryId ->
                    // A Join képernyő lekerül a stackről, így innen a vissza is a PantryDetail-re visz
                    navController.navigate(Screen.PantryItems(pantryId = joinedPantryId)) {
                        popUpTo<Screen.PantryJoin> { inclusive = true }
                    }
                }
            )
        }

        // 5. Új elem hozzáadása a kamrához
        composable<Screen.AddItemsPantry> { backStackEntry ->
            val route: Screen.AddItemsPantry = backStackEntry.toRoute()
            AddItemsPantryView(
                currentPantryId = route.pantryId,
                onBackClick = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.PantryItems(pantryId = route.pantryId))
                    }
                },
                onItemAdded = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.PantryItems(pantryId = route.pantryId))
                    }
                }
            )
        }

        // 6. Bevásárlólista ("Lista" fül) – a felhasználó egyetlen listája
        composable<Screen.CreateShoppingList> {
            ShoppingListDetailScreen(
                onBackClick = { navController.popBackStack() },
                onAddFromRecipesClick = { targetPantryId ->
                    navController.navigate(Screen.AddRecipesToShoppingList(targetPantryId = targetPantryId))
                }
            )
        }

        // 7. Receptek hiányzó hozzávalóinak listára tétele
        composable<Screen.AddRecipesToShoppingList> {
            CreateShoppingListScreen(
                onBackClick = { navController.popBackStack() },
                // Sikeres hozzáadás után vissza a listához
                onItemsAdded = { navController.popBackStack() }
            )
        }

        // 8. Receptek listája
        composable<Screen.RecipeList> {
            Scaffold { innerPadding ->
                RecipeListScreen(
                    viewModel = recipeViewModel,
                    onRecipeClick = { recipeId ->
                        navController.navigate(Screen.RecipeDetail(recipeId = recipeId))
                    },
                    modifier = Modifier
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                )
            }
        }

        // 9. Recept részletei
        composable<Screen.RecipeDetail> { backStackEntry ->
            val route: Screen.RecipeDetail = backStackEntry.toRoute()
            val recipes by recipeViewModel.allRecipes.collectAsStateWithLifecycle()
            val recipe = recipes.find { it.id == route.recipeId }
            // Az "Elkészítem" gomb: a hozzávalók levonása a kiválasztott kamra készletéből
            val cookViewModel: CookRecipeViewModel = viewModel()
            Scaffold { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                ) {
                    RecipeDetailScreen(
                        recipe = recipe,
                        onBackClick = { navController.popBackStack() },
                        onCookClick = { recipe?.let { cookViewModel.onCookClick(it.ingredients) } }
                    )
                    CookRecipeOverlay(viewModel = cookViewModel)
                }
            }
        }
    }
}