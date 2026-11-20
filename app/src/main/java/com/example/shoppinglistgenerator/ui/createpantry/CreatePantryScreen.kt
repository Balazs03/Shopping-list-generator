package com.example.shoppinglistgenerator.ui.createpantry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CreatePantryScreen(
    onBackClick: () -> Unit = {},
    onCreateClick: (name: String, joinCode: String) -> Unit = { _, _ -> }
) {
    val backgroundColor = Color(0xFFF7F7F2)
    val cardContainerLight = Color(0xFFECEEE4)
    val greenButtonColor = Color(0xFF3E583D)
    val textPrimary = Color(0xFF1E211D)
    val textSecondary = Color(0xFF5C615A)
    val borderLight = Color(0xFFE0E2D8)

    var pantryName by remember { mutableStateOf("Otthoni kamra") }
    var passCode by remember { mutableStateOf("KERT26") }

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
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Vissza",
                        tint = textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Új kamra",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Hozz létre egy kamrát, és a jelkóddal hívd meg azokat, akikkel közösen főztök.",
                fontSize = 14.sp,
                color = textSecondary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = pantryName,
                onValueChange = { pantryName = it },
                label = { Text("Kamra neve", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = greenButtonColor,
                    unfocusedBorderColor = borderLight
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = passCode,
                onValueChange = { passCode = it },
                label = { Text("Jelkód", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = greenButtonColor,
                    unfocusedBorderColor = borderLight
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Ezzel a kóddal csatlakozhatnak a többiek.",
                fontSize = 12.sp,
                color = textSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onCreateClick(pantryName, passCode) },
                enabled = pantryName.isNotBlank() && passCode.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = greenButtonColor),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Létrehozás", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                        imageVector = Icons.Outlined.Group,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Legfeljebb két kamra",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "A saját és a csatlakozással elért kamrák együtt számítanak bele a 2 kamrás keretbe.",
                            fontSize = 13.sp,
                            color = textSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.VerifiedUser,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Te leszel a tulajdonos. Csak te törölheted a kamrát; a tagok az alapanyagokat közösen szerkeszthetik.",
                    fontSize = 13.sp,
                    color = textSecondary,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreatePantryScreenPreview() {
    CreatePantryScreen()
}