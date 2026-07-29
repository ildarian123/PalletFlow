package com.palletflow.presentation.screens.inventory

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.Item
import com.palletflow.data.local.entity.ItemType
import com.palletflow.data.local.entity.Warehouse
import com.palletflow.domain.usecase.item.DeleteItemUseCase
import com.palletflow.domain.usecase.item.GetAllItemsUseCase
import com.palletflow.domain.usecase.warehouse.DeleteWarehouseUseCase
import com.palletflow.domain.usecase.warehouse.GetAllWarehousesUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {

    private lateinit var getAllItemsUseCase: GetAllItemsUseCase
    private lateinit var deleteItemUseCase: DeleteItemUseCase
    private lateinit var getAllWarehousesUseCase: GetAllWarehousesUseCase
    private lateinit var deleteWarehouseUseCase: DeleteWarehouseUseCase
    private lateinit var authPreferences: AuthPreferences
    private lateinit var viewModel: InventoryViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testItems = listOf(
        Item(id = "1", userId = "user1", name = "Apple", type = ItemType.PALLET),
        Item(id = "2", userId = "user1", name = "Banana", type = ItemType.SINGLE_PRODUCT)
    )

    private val testWarehouses = listOf(
        Warehouse(id = "1", userId = "user1", name = "Main")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getAllItemsUseCase = mockk()
        deleteItemUseCase = mockk()
        getAllWarehousesUseCase = mockk()
        deleteWarehouseUseCase = mockk()
        authPreferences = mockk()

        every { authPreferences.getUserId() } returns "user1"
        every { getAllItemsUseCase() } returns flowOf(testItems)
        every { getAllWarehousesUseCase() } returns flowOf(testWarehouses)

        viewModel = InventoryViewModel(
            getAllItemsUseCase,
            deleteItemUseCase,
            getAllWarehousesUseCase,
            deleteWarehouseUseCase,
            authPreferences
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load items and warehouses`() {
        assertEquals(testItems, viewModel.uiState.value.items)
        assertEquals(testWarehouses, viewModel.uiState.value.warehouses)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `onSearchQueryChange should filter items`() {
        // When
        viewModel.onSearchQueryChange("Apple")

        // Then
        assertEquals(1, viewModel.uiState.value.filteredItems.size)
        assertEquals("Apple", viewModel.uiState.value.filteredItems[0].name)
    }

    @Test
    fun `onTypeFilterChange should filter items`() {
        // When
        viewModel.onTypeFilterChange(ItemType.SINGLE_PRODUCT)

        // Then
        assertEquals(1, viewModel.uiState.value.filteredItems.size)
        assertEquals("Banana", viewModel.uiState.value.filteredItems[0].name)
    }

    @Test
    fun `deleteItem should call use case`() = runTest {
        // Given
        val item = testItems[0]
        coEvery { deleteItemUseCase(item) } returns Result.success(Unit)

        // When
        viewModel.deleteItem(item)

        // Then
        coVerify { deleteItemUseCase(item) }
    }
}
