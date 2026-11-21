package com.example.shoppinglistgenerator.screen.pantrydetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PantryUiItem(
    val id: String,
    val name: String,
    val ingredientCount: Int,
    val memberCount: Int,
    val isOwner: Boolean,
    val isDefaultPersonal: Boolean = false
)

@Composable
fun PantryDetailScreen(
    pantries: List<PantryUiItem> = listOf(
        PantryUiItem("1", "Saját kamra", 5, 1, isOwner = true, isDefaultPersonal = true),
        PantryUiItem("2", "Otthoni kamra", 8, 3, isOwner = false, isDefaultPersonal = false)
    ),
    onOpenPantry: (String) -> Unit = {},
    onDeletePantry: (String) -> Unit = {},
    onLeavePantry: (String) -> Unit = {},
    onAddPantryClick: () -> Unit = {},
    onJoinPantryClick: () -> Unit = {},
    onNavigateTab: (Int) -> Unit = {}
) {
    val backgroundColor = Color(0xFFF7F7F2)
    val cardContainerLight = Color(0xFFECEEE4)
    val badgeGreenBg = Color(0xFFD5E2CD)
    val badgeGreenText = Color(0xFF364E35)
    val primaryDarkGreen = Color(0xFF3E583D)
    val deleteRedText = Color(0xFFB3261E)
    val textPrimary = Color(0xFF1E211D)
    val textSecondary = Color(0xFF5C615A)
    val borderLight = Color(0xFFE0E2D8)

    val isLimitReached = pantries.size >= 2

    Scaffold(
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = textPrimary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Kamráim",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Alapanyagok egy helyen, magadnak és az otthoniaknak.",
                fontSize = 14.sp,
                color = textSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            pantries.forEach { pantry ->
                val subtitle = if (pantry.memberCount == 1) {
                    "${pantry.ingredientCount} alapanyag · csak te"
                } else {
                    "${pantry.ingredientCount} alapanyag · ${pantry.memberCount} tag"
                }

                val actionText = when {
                    pantry.isDefaultPersonal -> null
                    pantry.isOwner -> "Törlés"
                    else -> "Kilépés"
                }

                PantryCard(
                    title = pantry.name,
                    subtitle = subtitle,
                    badgeText = if (pantry.isOwner) "Tulajdonos vagy" else "Tag vagy",
                    actionText = actionText,
                    actionColor = if (pantry.isOwner) deleteRedText else primaryDarkGreen,
                    isPersonal = pantry.isDefaultPersonal,
                    onOpen = { onOpenPantry(pantry.id) },
                    onAction = {
                        if (pantry.isOwner) onDeletePantry(pantry.id)
                        else onLeavePantry(pantry.id)
                    },
                    badgeGreenBg = badgeGreenBg,
                    badgeGreenText = badgeGreenText,
                    cardContainerLight = cardContainerLight,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    borderLight = borderLight,
                    backgroundColor = backgroundColor
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (isLimitReached) {
                Surface(
                    color = cardContainerLight,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Mindkét hely foglalt",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Legfeljebb 2 kamrád lehet. Új kamrához előbb töröld a közös kamrádat, vagy lépj ki belőle.",
                                fontSize = 13.sp,
                                color = textSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAddPantryClick,
                    enabled = !isLimitReached,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryDarkGreen,
                        disabledContainerColor = Color(0xFFDCDCD6),
                        disabledContentColor = Color(0xFF9E9E98)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Új kamra", fontSize = 14.sp)
                }

                Button(
                    onClick = onJoinPantryClick,
                    enabled = !isLimitReached,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryDarkGreen,
                        disabledContainerColor = Color(0xFFDCDCD6),
                        disabledContentColor = Color(0xFF9E9E98)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Csatlakozás", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PantryCard(
    title: String,
    subtitle: String,
    badgeText: String,
    actionText: String?,
    actionColor: Color,
    isPersonal: Boolean,
    onOpen: () -> Unit,
    onAction: () -> Unit,
    badgeGreenBg: Color,
    badgeGreenText: Color,
    cardContainerLight: Color,
    textPrimary: Color,
    textSecondary: Color,
    borderLight: Color,
    backgroundColor: Color
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, borderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = cardContainerLight,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPersonal) Icons.Outlined.Inventory2 else Icons.Outlined.Group,
                            contentDescription = null,
                            tint = textSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = textPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = badgeGreenBg,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = badgeGreenText
                    )
                }

                if (actionText != null) {
                    TextButton(
                        onClick = onAction,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = actionText,
                            color = actionColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onOpen,
                border = BorderStroke(1.dp, borderLight),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = backgroundColor,
                    contentColor = textPrimary
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kamra megnyitása", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PantryDetailScreenPreview() {
    PantryDetailScreen()
}