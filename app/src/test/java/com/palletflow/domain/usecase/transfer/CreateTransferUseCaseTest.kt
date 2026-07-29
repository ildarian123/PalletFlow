package com.palletflow.domain.usecase.transfer

import android.util.Log
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.firestore.FirestoreSyncManager
import com.palletflow.data.local.dao.SaleItemDao
import com.palletflow.data.local.entity.Stock
import com.palletflow.domain.repository.StockRepository
import com.palletflow.domain.repository.TransactionRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateTransferUseCaseTest {

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var stockRepository: StockRepository
    private lateinit var saleItemDao: SaleItemDao
    private lateinit var authPreferences: AuthPreferences
    private lateinit var firestoreSyncManager: FirestoreSyncManager
    private lateinit var useCase: CreateTransferUseCase

    private val userId = "user1"

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        transactionRepository = mockk(relaxed = true)
        stockRepository = mockk(relaxed = true)
        saleItemDao = mockk(relaxed = true)
        authPreferences = mockk()
        firestoreSyncManager = mockk(relaxed = true)
        
        useCase = CreateTransferUseCase(
            transactionRepository,
            stockRepository,
            saleItemDao,
            authPreferences,
            firestoreSyncManager
        )

        every { authPreferences.getUserId() } returns userId
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `invoke should succeed when enough stock in source`() = runTest {
        // Given
        val items = listOf(
            ItemToTransfer("item1", "Item 1", "lot1", 5, "wh1", "Warehouse 1")
        )
        coEvery { stockRepository.getStock(userId, "item1", "wh1") } returns Stock(
            id = "stock1",
            userId = userId,
            itemId = "item1",
            warehouseId = "wh1",
            quantity = 10
        )
        coEvery { transactionRepository.createTransaction(any()) } returns Result.success(mockk())

        // When
        val result = useCase(
            transferNumber = "TR-001",
            sourceWarehouseId = "wh1",
            destWarehouseId = "wh2",
            items = items
        )

        // Then
        assertTrue(result.isSuccess)
        coVerify { stockRepository.decrementQuantity(userId, "item1", "wh1", 5) }
        coVerify { stockRepository.incrementQuantity(userId, "item1", "wh2", 5) }
    }

    @Test
    fun `invoke should fail when not enough stock in source`() = runTest {
        // Given
        val items = listOf(
            ItemToTransfer("item1", "Item 1", "lot1", 20, "wh1", "Warehouse 1")
        )
        coEvery { stockRepository.getStock(userId, "item1", "wh1") } returns Stock(
            id = "stock1",
            userId = userId,
            itemId = "item1",
            warehouseId = "wh1",
            quantity = 10
        )

        // When
        val result = useCase(
            transferNumber = "TR-001",
            sourceWarehouseId = "wh1",
            destWarehouseId = "wh2",
            items = items
        )

        // Then
        assertTrue(result.isFailure)
    }
}
