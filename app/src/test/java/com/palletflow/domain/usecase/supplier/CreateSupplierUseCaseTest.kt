package com.palletflow.domain.usecase.supplier

import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.domain.repository.SupplierRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateSupplierUseCaseTest {

    private lateinit var repository: SupplierRepository
    private lateinit var authPreferences: AuthPreferences
    private lateinit var useCase: CreateSupplierUseCase

    @Before
    fun setup() {
        repository = mockk()
        authPreferences = mockk()
        useCase = CreateSupplierUseCase(repository, authPreferences)
        
        every { authPreferences.getUserId() } returns "user1"
    }

    @Test
    fun `invoke should succeed when supplier does not exist`() = runTest {
        // Given
        every { repository.getAllSuppliers("user1") } returns flowOf(emptyList())
        coEvery { repository.createSupplier(any()) } returns Result.success(mockk())

        // When
        val result = useCase("Acme Corp")

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke should fail when supplier already exists`() = runTest {
        // Given
        val existing = mockk<com.palletflow.data.local.entity.Supplier>()
        every { existing.name } returns "Acme Corp"
        every { repository.getAllSuppliers("user1") } returns flowOf(listOf(existing))

        // When
        val result = useCase("Acme Corp")

        // Then
        assertTrue(result.isFailure)
    }
}
