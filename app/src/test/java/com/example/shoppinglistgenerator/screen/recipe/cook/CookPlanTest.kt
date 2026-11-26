package com.example.shoppinglistgenerator.screen.recipe.cook

import com.example.shoppinglistgenerator.model.Ingredient
import com.example.shoppinglistgenerator.model.dto.ItemDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CookPlanTest {

    private fun item(id: String, name: String, quantity: Double, unit: String) =
        ItemDto(itemId = id, pantry = "p", name = name, quantity = quantity, unit = unit)

    @Test
    fun `enough in pantry - deducted, nothing missing`() {
        val plan = planCooking(
            ingredients = listOf(Ingredient("Spagetti", 200.0, "g")),
            pantryItems = listOf(item("1", "Spagetti", 500.0, "g"))
        )
        assertEquals(1, plan.deductions.size)
        assertEquals(300.0, plan.deductions.single().newQuantity, 1e-6)
        assertTrue(plan.missing.isEmpty())
    }

    @Test
    fun `name matching ignores case and surrounding spaces`() {
        val plan = planCooking(
            ingredients = listOf(Ingredient("  fokhagyma ", 2.0, "db")),
            pantryItems = listOf(item("1", "Fokhagyma", 6.0, "DB"))
        )
        assertEquals(4.0, plan.deductions.single().newQuantity, 1e-6)
    }

    @Test
    fun `not enough - item goes to zero and the rest is missing`() {
        val plan = planCooking(
            ingredients = listOf(Ingredient("Tojás", 4.0, "db")),
            pantryItems = listOf(item("1", "Tojás", 1.0, "db"))
        )
        assertEquals(0.0, plan.deductions.single().newQuantity, 1e-6)
        assertEquals(3.0, plan.missing.single().quantity, 1e-6)
    }

    @Test
    fun `not in pantry at all - missing, no deduction`() {
        val plan = planCooking(
            ingredients = listOf(Ingredient("Bazsalikom", 10.0, "g")),
            pantryItems = listOf(item("1", "Rizs", 1000.0, "g"))
        )
        assertTrue(plan.deductions.isEmpty())
        assertEquals("Bazsalikom", plan.missing.single().name)
    }

    @Test
    fun `convertible units are converted - kg in pantry, g in recipe`() {
        val plan = planCooking(
            ingredients = listOf(Ingredient("Liszt", 250.0, "g")),
            pantryItems = listOf(item("1", "Liszt", 1.0, "kg"))
        )
        assertEquals(0.75, plan.deductions.single().newQuantity, 1e-6)
    }

    @Test
    fun `liquid units are converted - dl in recipe, l in pantry`() {
        val plan = planCooking(
            ingredients = listOf(Ingredient("Tej", 3.0, "dl")),
            pantryItems = listOf(item("1", "Tej", 0.2, "l"))
        )
        assertEquals(0.0, plan.deductions.single().newQuantity, 1e-6)
        val missing = plan.missing.single()
        assertEquals(1.0, missing.quantity, 1e-6)
        assertEquals("dl", missing.unit)
    }

    @Test
    fun `ml in recipe, l in pantry`() {
        val plan = planCooking(
            ingredients = listOf(Ingredient("Tej", 250.0, "ml")),
            pantryItems = listOf(item("1", "Tej", 1.0, "l"))
        )
        assertEquals(0.75, plan.deductions.single().newQuantity, 1e-6)
        assertTrue(plan.missing.isEmpty())
    }

    @Test
    fun `l in recipe, ml in pantry`() {
        val plan = planCooking(
            ingredients = listOf(Ingredient("Tej", 0.5, "l")),
            pantryItems = listOf(item("1", "Tej", 300.0, "ml"))
        )
        assertEquals(0.0, plan.deductions.single().newQuantity, 1e-6)
        val missing = plan.missing.single()
        assertEquals(0.2, missing.quantity, 1e-6)
        assertEquals("l", missing.unit)
    }

    @Test
    fun `ek in recipe, g in pantry - 1 ek is about 15 g`() {
        val plan = planCooking(
            ingredients = listOf(Ingredient("Cukor", 2.0, "ek")),
            pantryItems = listOf(item("1", "Cukor", 100.0, "g"))
        )
        assertEquals(70.0, plan.deductions.single().newQuantity, 1e-6)
        assertTrue(plan.missing.isEmpty())
    }

    @Test
    fun `ek in recipe, kg and dkg in pantry`() {
        val plan = planCooking(
            ingredients = listOf(Ingredient("Liszt", 4.0, "ek"), Ingredient("Só", 1.0, "ek")),
            pantryItems = listOf(item("1", "Liszt", 1.0, "kg"), item("2", "Só", 1.0, "dkg"))
        )
        assertEquals(0.94, plan.deductions.first { it.item.itemId == "1" }.newQuantity, 1e-6)
        // 1 dkg = 10 g só van, 15 g kellene -> elfogy, 5 g = 1/3 ek hiányzik
        assertEquals(0.0, plan.deductions.first { it.item.itemId == "2" }.newQuantity, 1e-6)
        assertEquals(1.0 / 3.0, plan.missing.single().quantity, 1e-6)
        assertEquals("ek", plan.missing.single().unit)
    }

    @Test
    fun `ek and tk convert to ml and l as well`() {
        val plan = planCooking(
            ingredients = listOf(Ingredient("Olaj", 2.0, "ek"), Ingredient("Ecet", 3.0, "tk")),
            pantryItems = listOf(item("1", "Olaj", 0.5, "l"), item("2", "Ecet", 100.0, "ml"))
        )
        assertEquals(0.47, plan.deductions.first { it.item.itemId == "1" }.newQuantity, 1e-6)
        assertEquals(85.0, plan.deductions.first { it.item.itemId == "2" }.newQuantity, 1e-6)
    }

    @Test
    fun `tk and ek convert to each other and written-out unit names work`() {
        assertEquals(1.0, convertUnit(3.0, "tk", "ek")!!, 1e-6)
        assertEquals(30.0, convertUnit(2.0, "evőkanál", "g")!!, 1e-6)
        assertEquals(15.0, convertUnit(1.0, "ek.", "ml")!!, 1e-6)
    }

    @Test
    fun `ml and g are still not converted directly`() {
        assertEquals(null, convertUnit(100.0, "ml", "g"))
    }

    @Test
    fun `incompatible units are not matched`() {
        val plan = planCooking(
            ingredients = listOf(Ingredient("Paradicsom", 400.0, "g")),
            pantryItems = listOf(item("1", "Paradicsom", 3.0, "db"))
        )
        assertTrue(plan.deductions.isEmpty())
        assertEquals(400.0, plan.missing.single().quantity, 1e-6)
    }

    @Test
    fun `same ingredient twice does not deduct the same stock twice`() {
        val plan = planCooking(
            ingredients = listOf(Ingredient("Só", 5.0, "g"), Ingredient("só", 10.0, "g")),
            pantryItems = listOf(item("1", "Só", 12.0, "g"))
        )
        assertEquals(0.0, plan.deductions.single().newQuantity, 1e-6)
        assertEquals(3.0, plan.missing.single().quantity, 1e-6)
    }
}
