package com.palletflow.domain.usecase.lot

import android.util.Log
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.Lot
import com.palletflow.domain.repository.LotRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReceiveLotUseCaseTest {

    private lateinit var lotRepository: LotRepository
    private lateinit var authPreferences: AuthPreferences
    private lateinit var receiveLotUseCase: ReceiveLotUseCase

    private val userId = "user123"

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        lotRepository = mockk()
        authPreferences = mockk()
        receiveLotUseCase = ReceiveLotUseCase(lotRepository, authPreferences)

        every { authPreferences.getUserId() } returns userId
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `when lotId is null, should call createLotWithItems`() = runTest {
        // Given
        val lotNumber = "LOT-001"
        val supplierId = "sup1"
        val warehouseId = "wh1"
        val purchasePrice = 100.0
        val items = listOf(ItemToReceive("item1", 5, 20.0))
        val expenses = emptyList<ExpenseToAdd>()
        
        coEvery { 
            lotRepository.createLotWithItems(any(), items, expenses) 
        } returns Result.success(mockk())

        // When
        val result = receiveLotUseCase(
            lotNumber = lotNumber,
            supplierId = supplierId,
            warehouseId = warehouseId,
            purchasePrice = purchasePrice,
            notes = "Test notes",
            items = items,
            expenses = expenses
        )

        // Then
        assertTrue(result.isSuccess)
        coVerify { lotRepository.createLotWithItems(any(), items, expenses) }
        coVerify(exactly = 0) { lotRepository.updateLotWithItems(any(), any(), any()) }
    }

    @Test
    fun `when lotId is provided, should call updateLotWithItems`() = runTest {
        // Given
        val lotId = "existing-lot-id"
        val lotNumber = "LOT-001"
        val supplierId = "sup1"
        val warehouseId = "wh1"
        val purchasePrice = 100.0
        val items = listOf(ItemToReceive("item1", 5, 20.0))
        val expenses = emptyList<ExpenseToAdd>()

        coEvery { 
            lotRepository.updateLotWithItems(any(), items, expenses) 
        } returns Result.success(mockk())

        // When
        val result = receiveLotUseCase(
            lotNumber = lotNumber,
            supplierId = supplierId,
            warehouseId = warehouseId,
            purchasePrice = purchasePrice,
            notes = "Test notes",
            items = items,
            expenses = expenses,
            lotId = lotId
        )

        // Then
        assertTrue(result.isSuccess)
        coVerify { lotRepository.updateLotWithItems(any(), items, expenses) }
        coVerify(exactly = 0) { lotRepository.createLotWithItems(any(), any(), any()) }
    }

    @Test
    fun `when user is not logged in, should return failure`() = runTest {
        // Given
        every { authPreferences.getUserId() } returns null

        // When
        val result = receiveLotUseCase(
            lotNumber = "LOT-001",
            supplierId = "sup1",
            warehouseId = "wh1",
            purchasePrice = 100.0,
            notes = null,
            items = listOf(ItemToReceive("item1", 1)),
            expenses = emptyList()
        )

        // Then
        assertTrue(result.isFailure)
        assertEquals("User not logged in", result.exceptionOrNull()?.message)
    }

    @Test
    fun `when lot number is blank, should return failure`() = runTest {
        // When
        val result = receiveLotUseCase(
            lotNumber = " ",
            supplierId = "sup1",
            warehouseId = "wh1",
            purchasePrice = 100.0,
            notes = null,
            items = listOf(ItemToReceive("item1", 1)),
            expenses = emptyList()
        )

        // Then
        assertTrue(result.isFailure)
        assertEquals("Lot number is required", result.exceptionOrNull()?.message)
    }
}
