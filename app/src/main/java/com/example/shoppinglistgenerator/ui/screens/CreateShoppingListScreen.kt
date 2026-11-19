package com.example.shoppinglistgenerator.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Új bevásárlólista: kamra választása, receptek kiválasztása, majd a "Készlet összetétele"
 * táblázat alapján csak a hiányzó mennyiségek kerülnek a listára.
 *
 * A pantryId-t a ViewModel a navigációs argumentumból (SavedStateHandle) is megkapja,
 * a paraméter a meglévő navigációs hívás kompatibilitása miatt maradt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateShoppingListScreen(
    pantryId: String? = null,
    onBackClick: () -> Unit = {},
    onListCreated: (String) -> Unit = {},
    viewModel: CreateShoppingListViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.createdListId) {
        state.createdListId?.let(onListCreated)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorShown()
        }
    }

    val selectedPantry = state.pantries.firstOrNull { it.id == state.selectedPantryId }

    Scaffold(
        containerColor = ShoppingListColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Új bevásárlólista",
                        fontWeight = FontWeight.Medium,
                        color = ShoppingListColors.OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Vissza",
                            tint = ShoppingListColors.Primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShoppingListColors.Background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ShoppingListColors.Background)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ShoppingPrimaryButton(
                    text = if (state.isCreating) "Létrehozás..." else "Hiányzó mennyiségek listára",
                    icon = Icons.Outlined.ShoppingCart,
                    enabled = state.canCreate,
                    onClick = viewModel::onCreateClick
                )
                TextButton(onClick = onBackClick) {
                    Text(
                        text = "Mégse",
                        fontWeight = FontWeight.SemiBold,
                        color = ShoppingListColors.Primary
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = ShoppingListColors.Primary
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Válassz kamrát, hogy a megfelelő bevásárló listára gyűljenek " +
                                    "a szükséges alapanyagok.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ShoppingListColors.OnSurfaceVariant
                        )
                    }

                    // --- Kamra választó ---
                    item {
                        if (state.pantries.isEmpty()) {
                            InfoCard(
                                "Nem találtunk kamrát, ezért a lista a kiválasztott receptek " +
                                        "összes hozzávalóját tartalmazza."
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    state.pantries.forEach { pantry ->
                                        PantryChoiceCard(
                                            name = pantry.name,
                                            selected = pantry.id == state.selectedPantryId,
                                            onClick = { viewModel.onPantrySelected(pantry.id) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                if (selectedPantry != null) {
                                    val members = maxOf(selectedPantry.memberCount, 1)
                                    Text(
                                        text = "A lista a(z) ${selectedPantry.name} mind a " +
                                                "$members tagja számára látható.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ShoppingListColors.OnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // --- Receptek ---
                    item { ShoppingSectionTitle("Receptek") }
                    if (state.recipes.isEmpty()) {
                        item {
                            Text(
                                text = "Nincs elérhető recept.",
                                color = ShoppingListColors.OnSurfaceVariant
                            )
                        }
                    }
                    items(state.recipes, key = { it.id }) { recipe ->
                        RecipeCard(
                            name = recipe.name,
                            ingredientCount = recipe.ingredientCount,
                            selected = recipe.id in state.selectedRecipeIds,
                            onClick = { viewModel.onRecipeToggled(recipe.id) }
                        )
                    }

                    // --- Készlet összetétele ---
                    item { ShoppingSectionTitle("Készlet összetétele") }
                    if (state.selectedRecipeIds.isEmpty()) {
                        item {
                            Text(
                                text = "Válassz legalább egy receptet, hogy lásd, mi hiányzik " +
                                        "a kamrából.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ShoppingListColors.OnSurfaceVariant
                            )
                        }
                    } else {
                        item { IngredientTable(rows = state.rows) }
                        item {
                            val missing = state.missingRows
                            Text(
                                text = if (missing.isEmpty()) {
                                    "Minden hozzávaló megvan a kamrában, nincs mit vásárolni."
                                } else {
                                    "Csak a hiányzó " + joinWithAnd(
                                        missing.map {
                                            "${formatAmount(it.missing)} ${it.unit} ${it.name.lowercase()}"
                                        }
                                    ) + " kerül a bevásárlólistára."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = ShoppingListColors.OnSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun joinWithAnd(parts: List<String>): String =
    if (parts.size <= 1) {
        parts.joinToString("")
    } else {
        parts.dropLast(1).joinToString(", ") + " és " + parts.last()
    }

@Composable
private fun PantryChoiceCard(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier
            .height(56.dp)
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = if (selected) ShoppingListColors.PrimaryContainer else ShoppingListColors.Surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) ShoppingListColors.Primary else ShoppingListColors.Outline
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = ShoppingListColors.Primary,
                    unselectedColor = ShoppingListColors.OnSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                fontWeight = FontWeight.Medium,
                color = ShoppingListColors.OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecipeCard(
    name: String,
    ingredientCount: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = if (selected) ShoppingListColors.PrimaryContainer else ShoppingListColors.Surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) ShoppingListColors.Primary else ShoppingListColors.Outline
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = selected,
                onCheckedChange = null,
                modifier = Modifier.padding(8.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = ShoppingListColors.Primary,
                    uncheckedColor = ShoppingListColors.OnSurface,
                    checkmarkColor = Color.White
                )
            )
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = ShoppingListColors.OnSurface
                )
                Text(
                    text = "$ingredientCount hozzávaló",
                    style = MaterialTheme.typography.bodySmall,
                    color = ShoppingListColors.Success
                )
            }
        }
    }
}

@Composable
private fun IngredientTable(rows: List<IngredientRowUi>) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ShoppingListColors.Surface,
        border = BorderStroke(1.dp, ShoppingListColors.Outline)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Alapanyag",
                    style = MaterialTheme.typography.labelSmall,
                    color = ShoppingListColors.OnSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Szükséges",
                    style = MaterialTheme.typography.labelSmall,
                    color = ShoppingListColors.OnSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(76.dp)
                )
                Text(
                    text = "Van otthon",
                    style = MaterialTheme.typography.labelSmall,
                    color = ShoppingListColors.OnSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(76.dp)
                )
            }

            if (rows.isEmpty()) {
                Text(
                    text = "A kiválasztott receptekhez nincs hozzávaló az adatbázisban.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ShoppingListColors.OnSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            rows.forEach { row ->
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = ShoppingListColors.Outline.copy(alpha = 0.6f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = row.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ShoppingListColors.OnSurface
                        )
                        if (row.missing > 0.0) {
                            Text(
                                text = "Hiányzik: ${formatAmount(row.missing)} ${row.unit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ShoppingListColors.Missing
                            )
                        } else {
                            Text(
                                text = "Elegendő a kamrában",
                                style = MaterialTheme.typography.bodySmall,
                                color = ShoppingListColors.Success
                            )
                        }
                    }
                    Text(
                        text = "${formatAmount(row.needed)} ${row.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShoppingListColors.OnSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(76.dp)
                    )
                    Text(
                        text = "${formatAmount(row.inPantry)} ${row.pantryUnit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShoppingListColors.OnSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(76.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard(text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ShoppingListColors.PrimaryContainer.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = ShoppingListColors.Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = ShoppingListColors.OnSurface
            )
        }
    }
}