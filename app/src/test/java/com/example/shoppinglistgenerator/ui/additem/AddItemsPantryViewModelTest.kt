package com.example.shoppinglistgenerator.ui.additem

import com.example.shoppinglistgenerator.model.dto.ItemDto
import com.example.shoppinglistgenerator.model.repository.ItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeItemRepository : ItemRepository() {
    val createdItems = mutableListOf<ItemDto>()
    var shouldFail = false

    override suspend fun createItem(item: ItemDto): Result<String> {
        if (shouldFail) {
            return Result.failure(Exception("Hiba történt"))
        }
        createdItems.add(item)
        return Result.success("fake-id-${createdItems.size}")
    }
}

class AddItemsPantryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeItemRepository: FakeItemRepository
    private lateinit var viewModel: AddItemsPantryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeItemRepository = FakeItemRepository()
        viewModel = AddItemsPantryViewModel(
            itemRepository = fakeItemRepository,
            currentPantryId = "otthoni",
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createItem_success_createsItemWithCorrectPantryAndTriggersCallback() {
        val selectedPantry = PantryOption(id = "sajat", name = "Saját", isShared = false)
        viewModel.onPantrySelected(selectedPantry)
        viewModel.onNameChanged("Búzaliszt")
        viewModel.onQuantityChanged("2")
        viewModel.onUnitSelected("kg")

        var onSuccessCalled = false
        viewModel.createItem { onSuccessCalled = true }

        assertTrue("onSuccess callback should be called", onSuccessCalled)
        assertEquals("One item should be created in repository", 1, fakeItemRepository.createdItems.size)

        val createdItem = fakeItemRepository.createdItems.first()
        assertEquals("Item name should match", "Búzaliszt", createdItem.name)
        assertEquals("Pantry ID should be set to the selected pantry", "sajat", createdItem.pantry)
        assertEquals("Quantity should match", 2.0, createdItem.quantity, 0.001)
        assertEquals("Unit should match", "kg", createdItem.unit)
        assertFalse("isPurchased should be false", createdItem.isPurchased)
        assertTrue("isSuccess should be true in UI state", viewModel.uiState.value.isSuccess)
    }

    @Test
    fun createItem_blankName_showsErrorMessageAndDoesNotCallRepository() {
        viewModel.onNameChanged("   ")

        var onSuccessCalled = false
        viewModel.createItem(onSuccess = { onSuccessCalled = true })

        assertFalse("onSuccess callback should NOT be called", onSuccessCalled)
        assertTrue("Repository should not contain any created item", fakeItemRepository.createdItems.isEmpty())

        val errorMessage = viewModel.uiState.value.errorMessage
        assertNotNull("Error message should not be null for blank name", errorMessage)
        assertEquals("Kérjük, add meg az alapanyag nevét!", errorMessage)
    }

    @Test
    fun onQuantityChanged_allowsDoubleWithDotAndComma_andParsesCorrectlyOnCreate() {
        // Test with dot decimal separator
        viewModel.onNameChanged("Tej")
        viewModel.onQuantityChanged("1.5")
        assertEquals("Quantity with dot should be accepted in UI state", "1.5", viewModel.uiState.value.quantity)

        viewModel.createItem()
        assertEquals("First created item quantity should be parsed as 1.5", 1.5, fakeItemRepository.createdItems.first().quantity, 0.001)

        // Test with comma decimal separator
        viewModel.onNameChanged("Cukor")
        viewModel.onQuantityChanged("2,75")
        assertEquals("Quantity with comma should be accepted in UI state", "2,75", viewModel.uiState.value.quantity)

        viewModel.createItem()
        assertEquals("Second created item quantity should be parsed as 2.75", 2.75, fakeItemRepository.createdItems[1].quantity, 0.001)
    }

    @Test
    fun onQuantityChanged_rejectsInvalidQuantityInput() {
        viewModel.onQuantityChanged("2.5")
        assertEquals("Initial valid quantity should be set", "2.5", viewModel.uiState.value.quantity)

        viewModel.onQuantityChanged("2.5abc")
        assertEquals("Invalid text input should be ignored and quantity unchanged", "2.5", viewModel.uiState.value.quantity)
    }
}