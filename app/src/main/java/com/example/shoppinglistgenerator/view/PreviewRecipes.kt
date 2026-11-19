package com.example.shoppinglistgenerator.view

import com.example.shoppinglistgenerator.model.Difficulty
import com.example.shoppinglistgenerator.model.Ingredient
import com.example.shoppinglistgenerator.model.Recipe

// Sample data for Compose previews only; the app loads its recipes from Firestore
internal val previewRecipes = listOf(
    Recipe(
        id = "paradicsomos-spagetti",
        name = "Paradicsomos spagetti",
        description = "Bazsalikommal és parmezánnal",
        prepTimeMinutes = 25,
        servings = 4,
        difficulty = Difficulty.EASY,
        ingredients = listOf(
            Ingredient("Spagetti", 400.0, "g"),
            Ingredient("Darabolt paradicsom", 400.0, "g"),
            Ingredient("Fokhagyma", 1.0, "db"),
            Ingredient("Olívaolaj", 30.0, "ml")
        ),
        steps = listOf(
            "Főzd a spagettit sós vízben 9–11 percig.",
            "Az olajon futtasd meg a fokhagymát, add hozzá a paradicsomot, és főzd 10 percig.",
            "Forgasd bele a tésztát. Parmezánnal tálald."
        )
    ),
    Recipe(
        id = "tejfolos-krumplileves",
        name = "Tejfölös krumplileves",
        description = "Egyszerű, melengető klasszikus",
        prepTimeMinutes = 40,
        servings = 4,
        difficulty = Difficulty.EASY
    )
)
