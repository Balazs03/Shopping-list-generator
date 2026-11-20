package com.example.shoppinglistgenerator.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.shoppinglistgenerator.screen.theme.DarkGreenPrimary
import com.example.shoppinglistgenerator.screen.theme.LightGreenSecondary
import com.example.shoppinglistgenerator.screen.theme.ScreenBackgroundColor
import com.example.shoppinglistgenerator.screen.theme.TextMutedColor

@Composable
fun MainAppScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Határozzuk meg, hogy melyik fül van éppen kiválasztva
    val isPantrySelected = currentDestination?.hasRoute(Screen.PantryItems::class) == true
    val isRecipesSelected = currentDestination?.hasRoute(Screen.RecipeList::class) == true
    val isShoppingListSelected = currentDestination?.hasRoute(Screen.CreateShoppingList::class) == true

    // Csak a top-level képernyőkön mutassuk a bottom navigation-t
    val showBottomBar = isPantrySelected || isRecipesSelected || isShoppingListSelected

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ScreenBackgroundColor,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFFEFF3EE),
                    tonalElevation = 2.dp
                ) {
                    NavigationBarItem(
                        selected = isPantrySelected,
                        onClick = {
                            if (!isPantrySelected) {
                                navController.navigate(Screen.PantryItems()) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Outlined.Archive, contentDescription = "Kamra") },
                        label = {
                            Text(
                                text = "Kamra",
                                fontWeight = if (isPantrySelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkGreenPrimary,
                            selectedTextColor = DarkGreenPrimary,
                            indicatorColor = LightGreenSecondary,
                            unselectedIconColor = TextMutedColor,
                            unselectedTextColor = TextMutedColor
                        )
                    )
                    NavigationBarItem(
                        selected = isRecipesSelected,
                        onClick = {
                            if (!isRecipesSelected) {
                                navController.navigate(Screen.RecipeList) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Outlined.Book, contentDescription = "Receptek") },
                        label = {
                            Text(
                                text = "Receptek",
                                fontWeight = if (isRecipesSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkGreenPrimary,
                            selectedTextColor = DarkGreenPrimary,
                            indicatorColor = LightGreenSecondary,
                            unselectedIconColor = TextMutedColor,
                            unselectedTextColor = TextMutedColor
                        )
                    )
                    NavigationBarItem(
                        selected = isShoppingListSelected,
                        onClick = {
                            if (!isShoppingListSelected) {
                                navController.navigate(Screen.CreateShoppingList()) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Outlined.ShoppingCart, contentDescription = "Lista") },
                        label = {
                            Text(
                                text = "Lista",
                                fontWeight = if (isShoppingListSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkGreenPrimary,
                            selectedTextColor = DarkGreenPrimary,
                            indicatorColor = LightGreenSecondary,
                            unselectedIconColor = TextMutedColor,
                            unselectedTextColor = TextMutedColor
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}