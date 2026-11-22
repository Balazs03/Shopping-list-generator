package com.example.shoppinglistgenerator.screen.recipe.cook

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shoppinglistgenerator.R
import com.example.shoppinglistgenerator.model.dto.ItemDto
import com.example.shoppinglistgenerator.model.dto.PantryDto
import com.example.shoppinglistgenerator.screen.theme.ShoppingListGeneratorTheme
import java.text.DecimalFormat

/**
 * Az "Elkészítem" gomb folyamatának megjelenítése: töltésjelző, kamraválasztó,
 * megerősítő dialógus és a végeredmény snackbar-ja. A recept képernyő fölé kell tenni.
 */
@Composable
fun CookRecipeOverlay(
    viewModel: CookRecipeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.messageRes) {
        uiState.messageRes?.let { res ->
            snackbarHostState.showSnackbar(context.getString(res, uiState.messageArg))
            viewModel.onMessageShown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.step == CookStep.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    when (val step = uiState.step) {
        is CookStep.ChoosePantry -> ChoosePantryDialog(
            pantries = step.pantries,
            onPantryChosen = viewModel::onPantryChosen,
            onDismiss = viewModel::onDismiss
        )
        is CookStep.Confirm -> ConfirmCookDialog(
            pantryName = step.pantry.name,
            plan = step.plan,
            isSaving = false,
            onConfirm = viewModel::onConfirm,
            onDismiss = viewModel::onDismiss
        )
        is CookStep.Saving -> ConfirmCookDialog(
            pantryName = step.pantry.name,
            plan = step.plan,
            isSaving = true,
            onConfirm = {},
            onDismiss = viewModel::onDismiss
        )
        CookStep.Idle, CookStep.Loading -> Unit
    }
}

@Composable
private fun ChoosePantryDialog(
    pantries: List<PantryDto>,
    onPantryChosen: (PantryDto) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.cook_choose_pantry_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.cook_choose_pantry_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                pantries.forEach { pantry ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPantryChosen(pantry) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = pantry.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.cook_pantry_members, pantry.users.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun ConfirmCookDialog(
    pantryName: String,
    plan: CookPlan,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val hasDeductions = plan.deductions.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.cook_confirm_title)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (hasDeductions) {
                        stringResource(R.string.cook_confirm_message, pantryName)
                    } else {
                        stringResource(R.string.cook_nothing_to_deduct, pantryName)
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                if (hasDeductions) {
                    Section(title = stringResource(R.string.cook_deducted_title)) {
                        plan.deductions.forEach { deduction ->
                            AmountRow(
                                name = deduction.item.name,
                                amount = stringResource(
                                    R.string.cook_amount_change,
                                    formatAmount(deduction.deductedAmount, deduction.item.unit),
                                    formatAmount(deduction.newQuantity, deduction.item.unit)
                                )
                            )
                        }
                    }
                }
                if (plan.missing.isNotEmpty()) {
                    Section(
                        title = stringResource(R.string.cook_missing_title),
                        titleColor = MaterialTheme.colorScheme.error
                    ) {
                        plan.missing.forEach { missing ->
                            AmountRow(
                                name = missing.name,
                                amount = formatAmount(missing.quantity, missing.unit)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (hasDeductions) {
                TextButton(onClick = onConfirm, enabled = !isSaving) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.cook_confirm_button))
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(if (hasDeductions) R.string.cancel else R.string.close))
            }
        }
    )
}

@Composable
private fun Section(
    title: String,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = titleColor
        )
        content()
    }
}

@Composable
private fun AmountRow(name: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

private fun formatAmount(quantity: Double, unit: String): String =
    "${DecimalFormat("#.##").format(quantity)} $unit".trim()

@Preview(showBackground = true)
@Composable
private fun ConfirmCookDialogPreview() {
    ShoppingListGeneratorTheme {
        ConfirmCookDialog(
            pantryName = "Otthoni kamra",
            plan = CookPlan(
                deductions = listOf(
                    PantryDeduction(ItemDto(itemId = "1", name = "Spagetti", quantity = 500.0, unit = "g"), 300.0),
                    PantryDeduction(ItemDto(itemId = "2", name = "Fokhagyma", quantity = 6.0, unit = "db"), 4.0)
                ),
                missing = listOf(MissingIngredient("Bazsalikom", 10.0, "g"))
            ),
            isSaving = false,
            onConfirm = {},
            onDismiss = {}
        )
    }
}
