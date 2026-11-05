package com.example.shoppinglistgenerator.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.shoppinglistgenerator.ui.additem.AddItemsPantryView
import com.example.shoppinglistgenerator.ui.screens.CreatePantryScreen
import com.example.shoppinglistgenerator.ui.screens.CreateShoppingListScreen
import com.example.shoppinglistgenerator.ui.screens.PantryDetailScreen
import com.example.shoppinglistgenerator.ui.screens.PantryItemsScreen
import com.example.shoppinglistgenerator.ui.screens.PantryJoinScreen
import com.example.shoppinglistgenerator.ui.screens.RecipeDetailScreen
import com.example.shoppinglistgenerator.ui.screens.RecipeListScreen
import com.example.shoppinglistgenerator.ui.screens.ShoppingListDetailScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Screen = Screen.PantryItems()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 1. Kamra elemei (Kezdőképernyő / Főképernyő)
        composable<Screen.PantryItems> { backStackEntry ->
            val route: Screen.PantryItems = backStackEntry.toRoute()
            PantryItemsScreen(
                pantryId = route.pantryId,
                onAddItemClick = {
                    navController.navigate(Screen.AddItemsPantry(pantryId = route.pantryId))
                },
                onPantryDetailClick = {
                    navController.navigate(Screen.PantryDetail(pantryId = route.pantryId))
                },
                onNavigateToRecipes = {
                    navController.navigate(Screen.RecipeList)
                },
                onNavigateToList = {
                    navController.navigate(Screen.CreateShoppingList(pantryId = route.pantryId))
                }
            )
        }

        // 2. Kamra létrehozása
        composable<Screen.CreatePantry> {
            CreatePantryScreen(
                onBackClick = { navController.popBackStack() },
                onPantryCreated = { newPantryId ->
                    navController.navigate(Screen.PantryItems(pantryId = newPantryId)) {
                        popUpTo<Screen.PantryItems> { inclusive = true }
                    }
                }
            )
        }

        // 3. Kamra részletei (Owner: törlés, Non-owner: kilépés)
        composable<Screen.PantryDetail> { backStackEntry ->
            val route: Screen.PantryDetail = backStackEntry.toRoute()
            PantryDetailScreen(
                pantryId = route.pantryId,
                onBackClick = { navController.popBackStack() },
                onPantryDeleted = {
                    navController.navigate(Screen.PantryItems()) {
                        popUpTo<Screen.PantryItems> { inclusive = true }
                    }
                },
                onLeftPantry = {
                    navController.navigate(Screen.PantryItems()) {
                        popUpTo<Screen.PantryItems> { inclusive = true }
                    }
                }
            )
        }

        // 4. Meglévő kamrához csatlakozás (jelkóddal)
        composable<Screen.PantryJoin> {
            PantryJoinScreen(
                onBackClick = { navController.popBackStack() },
                onPantryJoined = { joinedPantryId ->
                    navController.navigate(Screen.PantryItems(pantryId = joinedPantryId)) {
                        popUpTo<Screen.PantryItems> { inclusive = true }
                    }
                }
            )
        }

        // 5. Új elem hozzáadása a kamrához
        composable<Screen.AddItemsPantry> { backStackEntry ->
            val route: Screen.AddItemsPantry = backStackEntry.toRoute()
            AddItemsPantryView(
                currentPantryId = route.pantryId,
                onBackClick = { navController.popBackStack() },
                onItemAdded = { navController.popBackStack() },
                onNavigateToRecipes = {
                    navController.navigate(Screen.RecipeList)
                },
                onNavigateToList = {
                    navController.navigate(Screen.CreateShoppingList(pantryId = route.pantryId))
                }
            )
        }

        // 6. Bevásárlólista létrehozása
        composable<Screen.CreateShoppingList> { backStackEntry ->
            val route: Screen.CreateShoppingList = backStackEntry.toRoute()
            CreateShoppingListScreen(
                pantryId = route.pantryId,
                onBackClick = { navController.popBackStack() },
                onListCreated = { newListId ->
                    navController.navigate(Screen.ShoppingListDetail(listId = newListId)) {
                        popUpTo<Screen.CreateShoppingList> { inclusive = true }
                    }
                }
            )
        }

        // 7. Bevásárlólista részletei
        composable<Screen.ShoppingListDetail> { backStackEntry ->
            val route: Screen.ShoppingListDetail = backStackEntry.toRoute()
            ShoppingListDetailScreen(
                listId = route.listId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 8. Receptek listája
        composable<Screen.RecipeList> {
            RecipeListScreen(
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.RecipeDetail(recipeId = recipeId))
                },
                onNavigateToPantry = {
                    navController.navigate(Screen.PantryItems())
                },
                onNavigateToList = {
                    navController.navigate(Screen.CreateShoppingList())
                }
            )
        }

        // 9. Recept részletei
        composable<Screen.RecipeDetail> { backStackEntry ->
            val route: Screen.RecipeDetail = backStackEntry.toRoute()
            RecipeDetailScreen(
                recipeId = route.recipeId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}