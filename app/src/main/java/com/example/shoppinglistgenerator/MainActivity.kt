package com.example.shoppinglistgenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.shoppinglistgenerator.navigation.MainAppScreen
import com.example.shoppinglistgenerator.screen.theme.ShoppingListGeneratorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShoppingListGeneratorTheme {
                MainAppScreen()
            }
        }
    }
}
