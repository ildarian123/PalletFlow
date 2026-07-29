package com.palletflow.domain.usecase.warehouse

import com.palletflow.domain.repository.WarehouseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteWarehouseUseCaseTest {

    @Test
    fun `invoke should call repository deleteWarehouse`() = runTest {
        // Given
        val repository = mockk<WarehouseRepository>()
        val useCase = DeleteWarehouseUseCase(repository)
        val warehouseId = "wh1"
        val userId = "user1"
        
        coEvery { repository.deleteWarehouse(warehouseId, userId) } returns Result.success(Unit)

        // When
        val result = useCase(warehouseId, userId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { repository.deleteWarehouse(warehouseId, userId) }
    }
}
