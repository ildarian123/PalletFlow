package com.palletflow.domain.usecase.item

import com.palletflow.data.local.entity.Item
import com.palletflow.domain.repository.ItemRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteItemUseCaseTest {

    @Test
    fun `invoke should call repository deleteItem with deletePhoto true`() = runTest {
        // Given
        val repository = mockk<ItemRepository>()
        val useCase = DeleteItemUseCase(repository)
        val item = Item(id = "1", name = "Test Item")
        
        coEvery { repository.deleteItem(item, true) } returns Result.success(Unit)

        // When
        val result = useCase(item)

        // Then
        assertTrue(result.isSuccess)
        coVerify { repository.deleteItem(item, true) }
    }
}
