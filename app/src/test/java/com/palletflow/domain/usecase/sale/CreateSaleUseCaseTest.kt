package com.palletflow.domain.usecase.sale

import android.util.Log
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.firestore.FirestoreSyncManager
import com.palletflow.data.local.dao.SaleExpenseDao
import com.palletflow.data.local.dao.SaleItemDao
import com.palletflow.data.local.entity.Stock
import com.palletflow.data.local.entity.Transaction
import com.palletflow.domain.repository.LotRepository
import com.palletflow.domain.repository.StockRepository
import com.palletflow.domain.repository.TransactionRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateSaleUseCaseTest {

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var stockRepository: StockRepository
    private lateinit var lotRepository: LotRepository
    private lateinit var saleItemDao: SaleItemDao
    private lateinit var saleExpenseDao: SaleExpenseDao
    private lateinit var authPreferences: AuthPreferences
    private lateinit var firestoreSyncManager: FirestoreSyncManager
    private lateinit var createSaleUseCase: CreateSaleUseCase

    private val userId = "user123"

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        transactionRepository = mockk(relaxed = true)
        stockRepository = mockk(relaxed = true)
        lotRepository = mockk(relaxed = true)
        saleItemDao = mockk(relaxed = true)
        saleExpenseDao = mockk(relaxed = true)
        authPreferences = mockk()
        firestoreSyncManager = mockk(relaxed = true)
        
        createSaleUseCase = CreateSaleUseCase(
            transactionRepository,
            stockRepository,
            lotRepository,
            saleItemDao,
            saleExpenseDao,
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
    fun `when creating a new sale with enough stock, should succeed`() = runTest {
        // Given
        val saleNumber = "SALE-001"
        val platform = "eBay"
        val warehouseId = "wh1"
        val items = listOf(ItemToSell("item1", "Item 1", "lot1", 2, 50.0))
        val expenses = emptyList<ExpenseToAdd>()

        coEvery { stockRepository.getStock(userId, "item1", warehouseId) } returns Stock(
            id = "stock1",
            userId = userId,
            itemId = "item1",
            warehouseId = warehouseId,
            quantity = 10
        )
        coEvery { transactionRepository.createTransaction(any()) } returns Result.success(mockk())

        // When
        val result = createSaleUseCase(
            saleNumber = saleNumber,
            platform = platform,
            warehouseId = warehouseId,
            items = items,
            expenses = expenses
        )

        // Then
        assertTrue(result.isSuccess)
        coVerify { stockRepository.decrementQuantity(userId, "item1", warehouseId, 2) }
        coVerify { transactionRepository.createTransaction(any()) }
        coVerify { saleItemDao.upsertSaleItem(any()) }
        coVerify(atLeast = 1) { firestoreSyncManager.syncToCloud(any(), any(), any()) }
    }

    @Test
    fun `when creating a new sale with not enough stock, should return failure`() = runTest {
        // Given
        val saleNumber = "SALE-001"
        val platform = "eBay"
        val warehouseId = "wh1"
        val items = listOf(ItemToSell("item1", "Item 1", "lot1", 20, 50.0))
        
        coEvery { stockRepository.getStock(userId, "item1", warehouseId) } returns Stock(
            id = "stock1",
            userId = userId,
            itemId = "item1",
            warehouseId = warehouseId,
            quantity = 10
        )

        // When
        val result = createSaleUseCase(
            saleNumber = saleNumber,
            platform = platform,
            warehouseId = warehouseId,
            items = items,
            expenses = emptyList()
        )

        // Then
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { stockRepository.decrementQuantity(any(), any(), any(), any()) }
    }
}
