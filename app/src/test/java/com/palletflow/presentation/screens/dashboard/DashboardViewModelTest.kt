package com.palletflow.presentation.screens.dashboard

import android.util.Log
import com.palletflow.data.auth.GoogleAuthManager
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.dao.UserDao
import com.palletflow.data.local.entity.UserProfile
import com.palletflow.domain.repository.TransactionRepository
import com.palletflow.domain.usecase.report.FinancialOverview
import com.palletflow.domain.usecase.report.ObserveFinancialOverviewUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private lateinit var authPreferences: AuthPreferences
    private lateinit var userDao: UserDao
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var observeFinancialOverviewUseCase: ObserveFinancialOverviewUseCase
    private lateinit var googleAuthManager: GoogleAuthManager
    private lateinit var viewModel: DashboardViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        authPreferences = mockk(relaxed = true)
        userDao = mockk()
        transactionRepository = mockk()
        observeFinancialOverviewUseCase = mockk()
        googleAuthManager = mockk(relaxed = true)

        every { authPreferences.getUserId() } returns "user123"
        coEvery { userDao.getUserById("user123") } returns UserProfile(
            id = "user123",
            email = "test@test.com",
            businessName = "Test Business"
        )
        every { observeFinancialOverviewUseCase(any(), any()) } returns flowOf(
            FinancialOverview(
                totalIncoming = 100.0,
                totalRevenue = 600.0,
                totalProfit = 500.0,
                profitMargin = 83.3,
                transactionCount = 5
            )
        )
        every { transactionRepository.getAllTransactions("user123") } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    @Test
    fun `initial state should load user profile and metrics`() = runTest {
        // When
        viewModel = DashboardViewModel(
            authPreferences,
            userDao,
            transactionRepository,
            observeFinancialOverviewUseCase,
            googleAuthManager
        )
        advanceUntilIdle()

        // Then
        assertEquals("Test Business", viewModel.uiState.value.userName)
        assertEquals("$500.00", viewModel.uiState.value.totalProfit)
        assertEquals("5", viewModel.uiState.value.itemsSold)
    }

    @Test
    fun `logout should call googleAuthManager and clear credentials`() = runTest {
        // Given
        viewModel = DashboardViewModel(
            authPreferences,
            userDao,
            transactionRepository,
            observeFinancialOverviewUseCase,
            googleAuthManager
        )
        advanceUntilIdle()

        // When
        viewModel.logout()

        // Then
        verify { googleAuthManager.signOut() }
        verify { authPreferences.clearCredentials() }
    }
}
