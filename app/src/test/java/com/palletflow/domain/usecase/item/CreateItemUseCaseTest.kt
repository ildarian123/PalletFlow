package com.palletflow.domain.usecase.item

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.Item
import com.palletflow.data.local.entity.ItemType
import com.palletflow.domain.repository.ItemRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateItemUseCaseTest {

    private lateinit var repository: ItemRepository
    private lateinit var authPreferences: AuthPreferences
    private lateinit var useCase: CreateItemUseCase

    @Before
    fun setup() {
        repository = mockk()
        authPreferences = mockk()
        useCase = CreateItemUseCase(repository, authPreferences)
        
        every { authPreferences.getUserId() } returns "user1"
    }

    @Test
    fun `invoke should create item and return success`() = runTest {
        // Given
        coEvery { repository.createItem(any()) } returns Result.success(mockk())

        // When
        val result = useCase(
            name = "Test Item",
            type = ItemType.PALLET,
            sku = "SKU123",
            notes = "Note",
            photoPath = null
        )

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when user not logged in`() = runTest {
        // Given
        every { authPreferences.getUserId() } returns null

        // When
        val result = useCase(
            name = "Test Item",
            type = ItemType.PALLET,
            sku = "SKU123",
            notes = "Note",
            photoPath = null
        )

        // Then
        assertTrue(result.isFailure)
    }
}
