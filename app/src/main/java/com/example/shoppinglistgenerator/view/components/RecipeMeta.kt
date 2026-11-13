package com.example.shoppinglistgenerator.view.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.shoppinglistgenerator.R
import com.example.shoppinglistgenerator.model.Difficulty
import com.example.shoppinglistgenerator.model.Recipe

@StringRes
fun Difficulty.labelRes(): Int = when (this) {
    Difficulty.EASY -> R.string.difficulty_easy
    Difficulty.MEDIUM -> R.string.difficulty_medium
    Difficulty.HARD -> R.string.difficulty_hard
}

/** Builds the "25 perc · 4 adag" (optionally "· Könnyű") summary line of a recipe. */
@Composable
fun recipeMetaText(recipe: Recipe, showDifficulty: Boolean = false): String {
    val parts = buildList {
        add(stringResource(R.string.prep_time_minutes, recipe.prepTimeMinutes))
        add(stringResource(R.string.servings, recipe.servings))
        if (showDifficulty) add(stringResource(recipe.difficulty.labelRes()))
    }
    return parts.joinToString(" · ")
}
