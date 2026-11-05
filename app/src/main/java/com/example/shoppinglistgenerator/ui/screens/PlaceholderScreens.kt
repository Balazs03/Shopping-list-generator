package com.example.shoppinglistgenerator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePantryScreen(
    onBackClick: () -> Unit = {},
    onPantryCreated: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kamra létrehozása") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Vissza")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("CreatePantryScreen (Név és Jelkód kötelező)")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onPantryCreated("new_pantry_id") }) {
                Text("Kamra Létrehozása")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryDetailScreen(
    pantryId: String,
    onBackClick: () -> Unit = {},
    onPantryDeleted: () -> Unit = {},
    onLeftPantry: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kamra részletei") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Vissza")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("PantryDetailScreen (Kamra ID: $pantryId)")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onPantryDeleted) {
                Text("Kamra törlése (Owner)")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onLeftPantry) {
                Text("Kilépés a kamrából (Non-owner)")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryJoinScreen(
    onBackClick: () -> Unit = {},
    onPantryJoined: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Csatlakozás kamrához") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Vissza")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("PantryJoinScreen (Jelkód megadásával)")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onPantryJoined("joined_pantry_id") }) {
                Text("Csatlakozás")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryItemsScreen(
    pantryId: String,
    onAddItemClick: () -> Unit = {},
    onPantryDetailClick: () -> Unit = {},
    onNavigateToRecipes: () -> Unit = {},
    onNavigateToList: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kamra elemei") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("PantryItemsScreen (Kamra ID: $pantryId)")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAddItemClick) {
                Text("Új elem hozzáadása")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onPantryDetailClick) {
                Text("Kamra részletei")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onNavigateToRecipes) {
                Text("Receptekhez")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onNavigateToList) {
                Text("Bevásárlólistákhoz")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateShoppingListScreen(
    pantryId: String? = null,
    onBackClick: () -> Unit = {},
    onListCreated: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bevásárlólista létrehozása") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Vissza")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("CreateShoppingListScreen (Pantry ID: ${pantryId ?: "Saját"})")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onListCreated("new_list_id") }) {
                Text("Lista létrehozása")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListDetailScreen(
    listId: String,
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bevásárlólista részletei") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Vissza")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("ShoppingListDetailScreen (Lista ID: $listId)")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    onRecipeClick: (String) -> Unit = {},
    onNavigateToPantry: () -> Unit = {},
    onNavigateToList: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receptek") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("RecipeListScreen")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onRecipeClick("recipe_123") }) {
                Text("Recept részletei (#123)")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onNavigateToPantry) {
                Text("Kamrához")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onNavigateToList) {
                Text("Bevásárlólistákhoz")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recept részletei") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Vissza")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("RecipeDetailScreen (Recept ID: $recipeId)")
        }
    }
}