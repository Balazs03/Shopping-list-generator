package com.example.shoppinglistgenerator.screen.createpantry

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CreatePantryScreen(
    viewModel: CreatePantryViewModel = viewModel(),
    currentPantryCount: Int = 1,
    onBackClick: () -> Unit = {},
    onCreated: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isCreated) {
        if (uiState.isCreated) {
            onCreated()
        }
    }

    CreatePantryContent(
        pantryName = uiState.pantryName,
        joinCode = uiState.joinCode,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onPantryNameChange = viewModel::onPantryNameChange,
        onJoinCodeChange = viewModel::onJoinCodeChange,
        onBackClick = onBackClick,
        onCreateClick = {
            viewModel.createSharedPantry(currentPantryCount)
        }
    )
}

@Composable
fun CreatePantryContent(
    pantryName: String = "Otthoni kamra",
    joinCode: String = "KERT26",
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onPantryNameChange: (String) -> Unit = {},
    onJoinCodeChange: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    onCreateClick: () -> Unit = {}
) {
    val backgroundColor = Color(0xFFF7F7F2)
    val cardContainerLight = Color(0xFFECEEE4)
    val greenButtonColor = Color(0xFF3E583D)
    val textPrimary = Color(0xFF1E211D)
    val textSecondary = Color(0xFF5C615A)
    val borderLight = Color(0xFFE0E2D8)

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

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            OutlinedTextField(
                value = pantryName,
                onValueChange = onPantryNameChange,
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
                value = joinCode,
                onValueChange = onJoinCodeChange,
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
                onClick = onCreateClick,
                enabled = !isLoading && pantryName.isNotBlank() && joinCode.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = greenButtonColor),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Létrehozás", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
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
    CreatePantryContent()
}