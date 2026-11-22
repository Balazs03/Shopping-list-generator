package com.example.shoppinglistgenerator.screen.recipe.cook

import com.example.shoppinglistgenerator.model.Ingredient
import com.example.shoppinglistgenerator.model.dto.ItemDto

/** Egy kamratétel, amiből levonunk: a régi és az új mennyiség (a tétel saját mértékegységében). */
data class PantryDeduction(
    val item: ItemDto,
    val newQuantity: Double
) {
    val deductedAmount: Double get() = item.quantity - newQuantity
}

/** Egy hozzávaló, amiből a kamrában nincs (elég): ennyi hiányzik a recept mértékegységében. */
data class MissingIngredient(
    val name: String,
    val quantity: Double,
    val unit: String
)

data class CookPlan(
    val deductions: List<PantryDeduction>,
    val missing: List<MissingIngredient>
)

/**
 * Kiszámolja, mit kell levonni a kamrából a recept elkészítéséhez.
 *
 * - A hozzávalót név alapján (kis/nagybetű és szóközök nélkül) párosítjuk a kamratételekhez.
 * - Átváltható mértékegységeknél (g/dkg/kg, ml/cl/dl/l, ek/tk) átváltunk, máskülönben egyezniük kell.
 * - Ha nincs elég, a tétel 0-ra csökken (a kamra listája ezt "elfogyott"-ként jelzi),
 *   a hiányzó rész pedig a [CookPlan.missing] listába kerül.
 */
fun planCooking(ingredients: List<Ingredient>, pantryItems: List<ItemDto>): CookPlan {
    // Tételenként a még meglévő mennyiség, hogy két azonos hozzávaló ne vonja le kétszer ugyanazt
    val remaining = pantryItems.associate { it.itemId to it.quantity }.toMutableMap()
    val missing = mutableListOf<MissingIngredient>()

    for (ingredient in ingredients) {
        if (ingredient.quantity <= 0.0) continue
        // Minden számolás a recept mértékegységében történik
        var stillNeeded = ingredient.quantity

        val candidates = pantryItems.filter {
            it.name.normalized() == ingredient.name.normalized() &&
                convertUnit(1.0, it.unit, ingredient.unit) != null
        }
        for (item in candidates) {
            if (stillNeeded <= EPSILON) break
            val available = convertUnit(
                remaining.getValue(item.itemId).coerceAtLeast(0.0), item.unit, ingredient.unit
            ) ?: continue
            val taken = minOf(available, stillNeeded)
            remaining[item.itemId] = convertUnit(available - taken, ingredient.unit, item.unit) ?: continue
            stillNeeded -= taken
        }

        if (stillNeeded > EPSILON) {
            missing += MissingIngredient(
                name = ingredient.name,
                quantity = stillNeeded,
                unit = ingredient.unit
            )
        }
    }

    val deductions = pantryItems.mapNotNull { item ->
        val newQuantity = remaining.getValue(item.itemId)
        if (newQuantity < item.quantity - EPSILON) PantryDeduction(item, newQuantity) else null
    }
    return CookPlan(deductions = deductions, missing = missing)
}

private enum class Dimension { MASS, VOLUME }

// Mértékegység -> (dimenzió -> szorzó az alapegységhez: g, illetve ml)
private val unitTable: Map<String, Map<Dimension, Double>> = mapOf(
    "g" to mapOf(Dimension.MASS to 1.0),
    "dkg" to mapOf(Dimension.MASS to 10.0),
    "kg" to mapOf(Dimension.MASS to 1000.0),
    "ml" to mapOf(Dimension.VOLUME to 1.0),
    "cl" to mapOf(Dimension.VOLUME to 10.0),
    "dl" to mapOf(Dimension.VOLUME to 100.0),
    "l" to mapOf(Dimension.VOLUME to 1000.0),
    // Kanalak: közelítő középérték (1 ek ≈ 15 ml ≈ 15 g, 1 tk ≈ 5 ml ≈ 5 g),
    // ezért tömegre és térfogatra is átválthatók
    "ek" to mapOf(Dimension.MASS to SPOON_EK, Dimension.VOLUME to SPOON_EK),
    "tk" to mapOf(Dimension.MASS to SPOON_TK, Dimension.VOLUME to SPOON_TK)
)

// Kiírt és pontos alakok a rövidítésre
private val unitAliases = mapOf(
    "evőkanál" to "ek",
    "teáskanál" to "tk",
    "gramm" to "g",
    "kilogramm" to "kg",
    "deka" to "dkg",
    "liter" to "l",
    "deciliter" to "dl",
    "milliliter" to "ml"
)

private fun String.normalized() = trim().lowercase()

private fun normalizeUnit(unit: String): String {
    val u = unit.normalized().removeSuffix(".")
    return unitAliases[u] ?: u
}

/** [amount] átváltása [from] egységből [to] egységbe; null, ha nem átválthatók. */
internal fun convertUnit(amount: Double, from: String, to: String): Double? {
    val f = normalizeUnit(from)
    val t = normalizeUnit(to)
    if (f == t) return amount
    val fromFactors = unitTable[f] ?: return null
    val toFactors = unitTable[t] ?: return null
    // Elsőként a tömeget próbáljuk, utána a térfogatot
    val dimension = Dimension.entries.firstOrNull { it in fromFactors && it in toFactors } ?: return null
    return amount * fromFactors.getValue(dimension) / toFactors.getValue(dimension)
}

private const val SPOON_EK = 15.0
private const val SPOON_TK = 5.0
private const val EPSILON = 1e-9
