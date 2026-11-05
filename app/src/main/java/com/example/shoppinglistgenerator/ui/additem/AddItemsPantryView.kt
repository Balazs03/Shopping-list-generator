package com.example.shoppinglistgenerator.ui.additem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoppinglistgenerator.ui.theme.ShoppingListGeneratorTheme
import com.example.shoppinglistgenerator.ui.theme.ScreenBackgroundColor
import com.example.shoppinglistgenerator.ui.theme.DarkGreenPrimary
import com.example.shoppinglistgenerator.ui.theme.LightGreenSecondary
import com.example.shoppinglistgenerator.ui.theme.LightSageBanner
import com.example.shoppinglistgenerator.ui.theme.TextDarkColor
import com.example.shoppinglistgenerator.ui.theme.TextMutedColor
import com.example.shoppinglistgenerator.ui.theme.BorderLightColor

@Composable
fun AddItemsPantryView(
    modifier: Modifier = Modifier,
    viewModel: AddItemsPantryViewModel = viewModel(),
    currentPantryId: String = "otthoni",
    onBackClick: () -> Unit = {},
    onItemAdded: () -> Unit = {},
    onNavigateToRecipes: () -> Unit = {},
    onNavigateToList: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentPantryId) {
        viewModel.updateCurrentPantry(currentPantryId)
    }

    AddItemsPantryContent(
        uiState = uiState,
        modifier = modifier,
        onNameChanged = viewModel::onNameChanged,
        onQuantityChanged = viewModel::onQuantityChanged,
        onUnitSelected = viewModel::onUnitSelected,
        onPantrySelected = viewModel::onPantrySelected,
        onAddItemClick = { viewModel.createItem(onSuccess = onItemAdded) },
        onBackClick = onBackClick,
        onNavigateToRecipes = onNavigateToRecipes,
        onNavigateToList = onNavigateToList
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemsPantryContent(
    uiState: AddItemsPantryUiState,
    modifier: Modifier = Modifier,
    onNameChanged: (String) -> Unit = {},
    onQuantityChanged: (String) -> Unit = {},
    onUnitSelected: (String) -> Unit = {},
    onPantrySelected: (PantryOption) -> Unit = {},
    onAddItemClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onNavigateToRecipes: () -> Unit = {},
    onNavigateToList: () -> Unit = {}
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ScreenBackgroundColor,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFEFF3EE),
                tonalElevation = 2.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.Archive, contentDescription = "Kamra") },
                    label = { Text("Kamra", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkGreenPrimary,
                        selectedTextColor = DarkGreenPrimary,
                        indicatorColor = LightGreenSecondary,
                        unselectedIconColor = TextMutedColor,
                        unselectedTextColor = TextMutedColor
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToRecipes,
                    icon = { Icon(Icons.Outlined.Book, contentDescription = "Receptek") },
                    label = { Text("Receptek") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextMutedColor,
                        unselectedTextColor = TextMutedColor
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToList,
                    icon = { Icon(Icons.Outlined.ShoppingCart, contentDescription = "Lista") },
                    label = { Text("Lista") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextMutedColor,
                        unselectedTextColor = TextMutedColor
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Fejléc: Vissza nyíl + Cím
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Vissza",
                        tint = TextDarkColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Új alapanyag hozzáadás",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDarkColor
                )
            }

            // Alcím
            Text(
                text = "A ténylegesen meglévő mennyiséget add meg.",
                fontSize = 14.sp,
                color = TextMutedColor,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Név mező
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChanged,
                label = { Text("Név") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = DarkGreenPrimary,
                    unfocusedBorderColor = BorderLightColor,
                    focusedLabelColor = DarkGreenPrimary,
                    unfocusedLabelColor = TextMutedColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Mennyiség (Db) és Egység mezők egy sorban
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Db mező
                OutlinedTextField(
                    value = uiState.quantity,
                    onValueChange = onQuantityChanged,
                    label = { Text("Mennyiség") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = DarkGreenPrimary,
                        unfocusedBorderColor = BorderLightColor,
                        focusedLabelColor = DarkGreenPrimary,
                        unfocusedLabelColor = TextMutedColor
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Egység lenyíló mező
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = uiState.unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mértékegység") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = DarkGreenPrimary,
                            unfocusedBorderColor = BorderLightColor,
                            focusedLabelColor = DarkGreenPrimary,
                            unfocusedLabelColor = TextMutedColor
                        ),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        uiState.availableUnits.forEach { unitOption ->
                            DropdownMenuItem(
                                text = { Text(unitOption) },
                                onClick = {
                                    onUnitSelected(unitOption)
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Kamra kiválasztás szakasz
            Text(
                text = "Kamra",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDarkColor,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.availablePantries.forEach { pantryOption ->
                    val isSelected = uiState.selectedPantryId == pantryOption.id

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) LightGreenSecondary else Color.White,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) DarkGreenPrimary else BorderLightColor
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clickable { onPantrySelected(pantryOption) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null, // Surface clickable kezeli
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = DarkGreenPrimary,
                                    unselectedColor = TextMutedColor
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = pantryOption.name,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = TextDarkColor
                            )
                        }
                    }
                }
            }

            // Dinamikus tájékoztató szöveg a kiválasztott kamráról
            Text(
                text = "Az ${uiState.selectedPantryName} kamrába kerül.",
                fontSize = 13.sp,
                color = TextMutedColor,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Hibaüzenet (ha van)
            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Hozzáadás gomb
            Button(
                onClick = onAddItemClick,
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkGreenPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Hozzáadás",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Információs kártya (Közösen kezelhető készlet)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = LightSageBanner,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Információ",
                        tint = DarkGreenPrimary,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Közösen kezelhető készlet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Az ${uiState.selectedPantryName} kamra minden tagja láthatja és szerkesztheti ezt az alapanyagot. A készlet nem változik automatikusan főzéskor.",
                            fontSize = 13.sp,
                            color = Color(0xFF4A564B),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddItemsPantryViewPreview() {
    ShoppingListGeneratorTheme {
        AddItemsPantryContent(
            uiState = AddItemsPantryUiState(
                name = "Búzaliszt",
                quantity = "2",
                unit = "kg",
                selectedPantryId = "otthoni",
                selectedPantryName = "Otthoni",
                availablePantries = listOf(
                    PantryOption(id = "sajat", name = "Saját", isShared = false),
                    PantryOption(id = "otthoni", name = "Otthoni", isShared = true)
                ),
                availableUnits = listOf("kg", "g", "l", "db")
            )
        )
    }
}