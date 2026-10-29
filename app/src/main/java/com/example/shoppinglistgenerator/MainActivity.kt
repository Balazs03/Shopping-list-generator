package com.example.shoppinglistgenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.shoppinglistgenerator.ui.theme.ShoppingListGeneratorTheme
import com.example.shoppinglistgenerator.view.ShoppingListApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShoppingListGeneratorTheme {
                ShoppingListApp()
            }
        }
    }
}
