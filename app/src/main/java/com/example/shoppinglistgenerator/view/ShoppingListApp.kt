package com.example.shoppinglistgenerator.view

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.shoppinglistgenerator.R
import com.example.shoppinglistgenerator.viewmodel.RecipeViewModel

private object Routes {
    const val PANTRY = "pantry"
    const val RECIPES = "recipes"
    const val RECIPE_LIST = "recipe_list"
    const val RECIPE_ID_ARG = "recipeId"
    const val RECIPE_DETAIL = "recipe/{$RECIPE_ID_ARG}"
    const val SHOPPING_LIST = "shopping_list"

    fun recipeDetail(recipeId: String) = "recipe/$recipeId"
}

private enum class BottomNavItem(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    PANTRY(Routes.PANTRY, R.string.nav_pantry, Icons.Outlined.Inventory2),
    RECIPES(Routes.RECIPES, R.string.nav_recipes, Icons.AutoMirrored.Outlined.MenuBook),
    SHOPPING_LIST(Routes.SHOPPING_LIST, R.string.nav_shopping_list, Icons.Outlined.ShoppingBasket)
}

@Composable
fun ShoppingListApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    // Activity-scoped, shared by the recipe list and the recipe detail screen
    val recipeViewModel: RecipeViewModel = viewModel()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                BottomNavItem.entries.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = item.icon, contentDescription = null) },
                        label = { Text(stringResource(item.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.RECIPES,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(Routes.PANTRY) {
                PlaceholderScreen(title = stringResource(R.string.nav_pantry))
            }
            navigation(startDestination = Routes.RECIPE_LIST, route = Routes.RECIPES) {
                composable(Routes.RECIPE_LIST) {
                    RecipeListScreen(
                        viewModel = recipeViewModel,
                        onRecipeClick = { recipeId ->
                            navController.navigate(Routes.recipeDetail(recipeId))
                        }
                    )
                }
                composable(
                    route = Routes.RECIPE_DETAIL,
                    arguments = listOf(navArgument(Routes.RECIPE_ID_ARG) { type = NavType.StringType })
                ) { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getString(Routes.RECIPE_ID_ARG)
                    RecipeDetailScreen(
                        recipe = recipeId?.let(recipeViewModel::getRecipeById),
                        onBackClick = { navController.navigateUp() },
                        // TODO: add the ingredients to the shopping list / pantry
                        onCookClick = {}
                    )
                }
            }
            composable(Routes.SHOPPING_LIST) {
                PlaceholderScreen(title = stringResource(R.string.nav_shopping_list))
            }
        }
    }
}
