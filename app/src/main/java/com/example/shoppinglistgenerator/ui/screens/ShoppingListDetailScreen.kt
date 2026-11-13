package com.example.shoppinglistgenerator.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Bevásárlólista részletei: előrehaladás, Összes / Hiányzó szűrő, kipipálható tételek,
 * új tétel hozzáadása, meglévő tétel szerkesztése / törlése.
 *
 * A listId-t a ViewModel a navigációs argumentumból (SavedStateHandle) kapja,
 * a paraméterek a meglévő navigációs hívás kompatibilitása miatt maradtak.
 */
@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListDetailScreen(
    listId: String,
    onBackClick: () -> Unit = {},
    viewModel: ShoppingListDetailViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ShoppingListItemUi?>(null) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorShown()
        }
    }

    Scaffold(
        containerColor = ShoppingListColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bevásárlólistám",
                        fontWeight = FontWeight.Medium,
                        color = ShoppingListColors.OnSurface
                    )
                },
                navigationIcon = {
                    Icon(
                        Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        tint = ShoppingListColors.OnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShoppingListColors.Background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            } else if (state.listNotFound) {
                Text(
                    text = "Ez a lista nem található.",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = ShoppingListColors.OnSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // --- Előrehaladás ---
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "${state.purchasedCount}/${state.items.size} kész",
                                style = MaterialTheme.typography.bodySmall,
                                color = ShoppingListColors.OnSurfaceVariant,
                                modifier = Modifier.align(Alignment.End)
                            )
                            LinearProgressIndicator(
                                progress = { state.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = ShoppingListColors.Primary,
                                trackColor = ShoppingListColors.Track
                            )
                        }
                    }

                    // --- Szűrő: Összes / Hiányzó ---
                    item {
                        FilterToggle(
                            showOnlyRemaining = state.showOnlyRemaining,
                            onChange = viewModel::onFilterChanged
                        )
                    }

                    item { ShoppingSectionTitle("Kipipálható elemek") }

                    // --- Tételek ---
                    item {
                        if (state.visibleItems.isEmpty()) {
                            Text(
                                text = if (state.items.isEmpty()) {
                                    "A listán még nincs tétel."
                                } else {
                                    "Minden tételt beszereztél. 🎉"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = ShoppingListColors.OnSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = ShoppingListColors.Surface,
                                border = BorderStroke(1.dp, ShoppingListColors.Outline)
                            ) {
                                Column {
                                    state.visibleItems.forEachIndexed { index, item ->
                                        key(item.id) {
                                            ShoppingItemRow(
                                                item = item,
                                                onCheckedChange = { checked ->
                                                    viewModel.onItemChecked(item.id, checked)
                                                },
                                                onEditClick = { editingItem = item }
                                            )
                                        }
                                        if (index < state.visibleItems.lastIndex) {
                                            HorizontalDivider(
                                                color = ShoppingListColors.Outline.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- Hozzáadás ---
                    item {
                        ShoppingPrimaryButton(
                            text = "Hozzáadás",
                            icon = Icons.Outlined.Add,
                            onClick = { showAddDialog = true },
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        ItemEditorDialog(
            title = "Új tétel hozzáadása",
            confirmLabel = "Hozzáadás",
            onConfirm = { name, quantity, unit ->
                viewModel.addItem(name, quantity, unit)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    editingItem?.let { item ->
        ItemEditorDialog(
            title = "Tétel szerkesztése",
            confirmLabel = "Mentés",
            initialName = item.name,
            initialQuantity = item.quantity,
            initialUnit = item.unit,
            onConfirm = { name, quantity, unit ->
                viewModel.updateItem(item.id, name, quantity, unit)
                editingItem = null
            },
            onDismiss = { editingItem = null },
            onDelete = {
                viewModel.deleteItem(item.id)
                editingItem = null
            }
        )
    }
}

@Composable
private fun FilterToggle(
    showOnlyRemaining: Boolean,
    onChange: (Boolean) -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(shape)
            .background(ShoppingListColors.Surface)
            .border(1.dp, ShoppingListColors.Outline, shape)
    ) {
        ToggleOption(
            label = "Összes",
            selected = !showOnlyRemaining,
            modifier = Modifier.weight(1f),
            onClick = { onChange(false) }
        )
        ToggleOption(
            label = "Hiányzó",
            selected = showOnlyRemaining,
            modifier = Modifier.weight(1f),
            onClick = { onChange(true) }
        )
    }
}

@Composable
private fun ToggleOption(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(if (selected) ShoppingListColors.PrimaryContainer else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selected) {
                Icon(
                    Icons.Outlined.Check,
                    contentDescription = null,
                    tint = ShoppingListColors.Primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                color = ShoppingListColors.OnSurface
            )
        }
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingListItemUi,
    onCheckedChange: (Boolean) -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!item.purchased) }
            .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.purchased,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = ShoppingListColors.Primary,
                uncheckedColor = ShoppingListColors.OnSurface,
                checkmarkColor = Color.White
            )
        )
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyLarge,
            color = if (item.purchased) {
                ShoppingListColors.OnSurfaceVariant
            } else {
                ShoppingListColors.OnSurface
            },
            textDecoration = if (item.purchased) TextDecoration.LineThrough else null,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        )
        Text(
            text = formatAmount(item.quantity),
            style = MaterialTheme.typography.bodyLarge,
            color = ShoppingListColors.OnSurface
        )
        Text(
            text = item.unit,
            fontSize = 12.sp,
            color = ShoppingListColors.OnSurfaceVariant,
            modifier = Modifier
                .width(28.dp)
                .padding(start = 4.dp)
        )
        IconButton(onClick = onEditClick) {
            Icon(
                Icons.Outlined.Edit,
                contentDescription = "Szerkesztés",
                tint = ShoppingListColors.OnSurfaceVariant
            )
        }
    }
}

private val UNIT_OPTIONS = listOf("g", "kg", "ml", "l", "db")

/** Közös párbeszédablak az új tétel felvételéhez és a meglévő szerkesztéséhez / törléséhez. */
@Composable
private fun ItemEditorDialog(
    title: String,
    confirmLabel: String,
    initialName: String = "",
    initialQuantity: Double? = null,
    initialUnit: String = "db",
    onConfirm: (name: String, quantity: Double, unit: String) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialName) }
    var quantityText by remember {
        mutableStateOf(initialQuantity?.let { formatAmount(it) } ?: "")
    }
    var unit by remember { mutableStateOf(initialUnit.ifBlank { "db" }) }

    val quantity = quantityText.replace(',', '.').toDoubleOrNull()
    val valid = name.isNotBlank() && quantity != null && quantity > 0.0
    val unitOptions = (UNIT_OPTIONS + unit).distinct()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ShoppingListColors.Surface,
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Név") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Mennyiség") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    unitOptions.forEach { option ->
                        UnitChip(
                            label = option,
                            selected = option == unit,
                            onClick = { unit = option }
                        )
                    }
                }
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = ShoppingListColors.Danger,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tétel törlése", color = ShoppingListColors.Danger)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { if (quantity != null) onConfirm(name.trim(), quantity, unit) }
            ) {
                Text(confirmLabel, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Mégse") }
        }
    )
}

@Composable
private fun UnitChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = if (selected) ShoppingListColors.PrimaryContainer else ShoppingListColors.Surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) ShoppingListColors.Primary else ShoppingListColors.Outline
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = ShoppingListColors.OnSurface
        )
    }
}

// ---------------------------------------------------------------------------
// Közös stílus és segédek (a CreateShoppingListScreen is használja)
// ---------------------------------------------------------------------------

/**
 * A Figma prototípusból becsült színek a két bevásárlólistás képernyőhöz.
 * Ha a ui/theme mappában már vannak a prototípus színei, ezeket cseréld azokra.
 */
internal object ShoppingListColors {
    val Background = Color(0xFFFAF8F2)
    val Surface = Color.White
    val Outline = Color(0xFFD8D9D0)
    val Primary = Color(0xFF466547)
    val PrimaryContainer = Color(0xFFDCE7D5)
    val OnSurface = Color(0xFF1C1C18)
    val OnSurfaceVariant = Color(0xFF5F625A)
    val Success = Color(0xFF3F6B43)
    val Missing = Color(0xFF9A5B2B)
    val Danger = Color(0xFFA5362B)
    val Track = Color(0xFFE2E4DB)
}

/** Teljes szélességű, sötétzöld, kerekített gomb (Hozzáadás, Hiányzó mennyiségek listára...). */
@Composable
internal fun ShoppingPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ShoppingListColors.Primary,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFD3D8CE),
            disabledContentColor = Color(0xFF8A8F85)
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun ShoppingSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = ShoppingListColors.OnSurface,
        modifier = modifier
    )
}

/** Mennyiség megjelenítése: 150 -> "150", 0.5 -> "0,5". */
internal fun formatAmount(value: Double): String {
    val rounded = Math.round(value * 100) / 100.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toLong().toString()
    } else {
        rounded.toString().replace('.', ',')
    }
}