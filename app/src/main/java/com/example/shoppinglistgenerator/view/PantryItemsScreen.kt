package com.example.shoppinglistgenerator.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoppinglistgenerator.R
import com.example.shoppinglistgenerator.model.dto.ItemDto
import com.example.shoppinglistgenerator.ui.theme.ShoppingListGeneratorTheme
import com.example.shoppinglistgenerator.viewmodel.PantryItemsUiState
import com.example.shoppinglistgenerator.viewmodel.PantryItemsViewModel
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@Composable
fun PantryItemsScreen(
    pantryId: String,
    onBackClick: () -> Unit,
    // Navigáció még nincs bekötve: szerkesztés és új tétel felvétele
    onEditItemClick: (itemId: String) -> Unit,
    onAddItemClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PantryItemsViewModel = viewModel(key = pantryId) { PantryItemsViewModel(pantryId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val deleteFailedMessage = stringResource(R.string.pantry_items_delete_failed)

    LaunchedEffect(uiState.deleteFailed) {
        if (uiState.deleteFailed) {
            snackbarHostState.showSnackbar(deleteFailedMessage)
            viewModel.onDeleteErrorShown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PantryItemsContent(
            uiState = uiState,
            onBackClick = onBackClick,
            onEditItemClick = onEditItemClick,
            onDeleteItemRequest = viewModel::onDeleteRequest,
            onAddItemClick = onAddItemClick
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    uiState.itemPendingDelete?.let { item ->
        DeleteItemDialog(
            itemName = item.name,
            onConfirm = viewModel::onDeleteConfirm,
            onDismiss = viewModel::onDeleteDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryItemsContent(
    uiState: PantryItemsUiState,
    onBackClick: () -> Unit,
    onEditItemClick: (String) -> Unit,
    onDeleteItemRequest: (ItemDto) -> Unit,
    onAddItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = uiState.pantryName.ifBlank { stringResource(R.string.pantry_items_default_title) },
                    fontWeight = FontWeight.SemiBold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            when {
                uiState.isLoading -> CenteredBox { CircularProgressIndicator() }
                uiState.loadFailed -> CenteredBox {
                    MutedText(stringResource(R.string.pantry_items_load_failed))
                }
                else -> {
                    ItemsHeader(
                        itemCount = uiState.items.size,
                        memberCount = uiState.memberCount
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (uiState.items.isEmpty()) {
                        CenteredBox(modifier = Modifier.weight(1f)) {
                            MutedText(stringResource(R.string.pantry_items_empty))
                        }
                    } else {
                        ItemsCard(
                            items = uiState.items,
                            onEditItemClick = onEditItemClick,
                            onDeleteItemRequest = onDeleteItemRequest,
                            // fill = false: kevés tételnél a kártya ne nyúljon le a gombig
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onAddItemClick,
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.pantry_items_add_button),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ItemsHeader(
    itemCount: Int,
    memberCount: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.pantry_items_heading),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = stringResource(R.string.pantry_items_count, itemCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.pantry_items_subtitle, memberCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ItemsCard(
    items: List<ItemDto>,
    onEditItemClick: (String) -> Unit,
    onDeleteItemRequest: (ItemDto) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        LazyColumn {
            itemsIndexed(items, key = { _, item -> item.itemId }) { index, item ->
                SwipeToDeleteRow(onDelete = { onDeleteItemRequest(item) }) {
                    ItemRow(
                        item = item,
                        onClick = { onEditItemClick(item.itemId) }
                    )
                }
                if (index < items.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun SwipeToDeleteRow(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val state = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()

    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        onDismiss = {
            // A sor visszaugrik, a tényleges törlés a megerősítő dialógus után történik
            onDelete()
            scope.launch { state.reset() }
        },
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.pantry_items_delete),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    ) {
        content()
    }
}

@Composable
private fun ItemRow(
    item: ItemDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = quantityFormat.format(item.quantity),
            style = MaterialTheme.typography.bodyLarge,
            // Elfogyott alapanyag kiemelése
            color = if (item.quantity <= 0.0) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        Text(
            text = item.unit,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .width(36.dp)
                .padding(start = 8.dp)
        )
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = stringResource(R.string.pantry_items_edit),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DeleteItemDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pantry_items_delete_title)) },
        text = { Text(stringResource(R.string.pantry_items_delete_message, itemName)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.pantry_items_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun CenteredBox(
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun MutedText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private val quantityFormat = DecimalFormat("#.##")

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PantryItemsContentPreview() {
    ShoppingListGeneratorTheme {
        Surface {
            PantryItemsContent(
                uiState = PantryItemsUiState(
                    pantryName = "Otthoni kamra",
                    memberCount = 3,
                    items = previewPantryItems,
                    isLoading = false
                ),
                onBackClick = {},
                onEditItemClick = {},
                onDeleteItemRequest = {},
                onAddItemClick = {}
            )
        }
    }
}

private val previewPantryItems = listOf(
    ItemDto(itemId = "1", name = "Spagetti", quantity = 250.0, unit = "g"),
    ItemDto(itemId = "2", name = "Darabolt paradicsom", quantity = 400.0, unit = "g"),
    ItemDto(itemId = "3", name = "Fokhagyma", quantity = 6.0, unit = "db"),
    ItemDto(itemId = "4", name = "Olívaolaj", quantity = 250.0, unit = "ml"),
    ItemDto(itemId = "5", name = "Bazsalikom", quantity = 0.0, unit = "g"),
    ItemDto(itemId = "6", name = "Parmezán", quantity = 20.0, unit = "g"),
    ItemDto(itemId = "7", name = "Rizs", quantity = 1000.0, unit = "g"),
    ItemDto(itemId = "8", name = "Tojás", quantity = 6.0, unit = "db")
)
