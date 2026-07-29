package com.palletflow.presentation.screens.auth

import android.util.Patterns
import android.util.Log
import com.palletflow.data.auth.GoogleAuthManager
import com.palletflow.data.backup.GoogleDriveBackupManager
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.firestore.FirestoreSyncManager
import com.palletflow.data.local.dao.*
import com.palletflow.data.local.entity.UserProfile
import com.palletflow.util.NetworkHelper
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.regex.Pattern

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var authPreferences: AuthPreferences
    private lateinit var userDao: UserDao
    private lateinit var googleAuthManager: GoogleAuthManager
    private lateinit var googleDriveBackupManager: GoogleDriveBackupManager
    private lateinit var firestoreSyncManager: FirestoreSyncManager
    private lateinit var networkHelper: NetworkHelper
    private lateinit var viewModel: LoginViewModel
    
    // DAOs
    private val itemDao: ItemDao = mockk(relaxed = true)
    private val warehouseDao: WarehouseDao = mockk(relaxed = true)
    private val supplierDao: SupplierDao = mockk(relaxed = true)
    private val lotDao: LotDao = mockk(relaxed = true)
    private val lotItemDao: LotItemDao = mockk(relaxed = true)
    private val lotExpenseDao: LotExpenseDao = mockk(relaxed = true)
    private val transactionDao: TransactionDao = mockk(relaxed = true)
    private val stockDao: StockDao = mockk(relaxed = true)
    private val saleItemDao: SaleItemDao = mockk(relaxed = true)
    private val saleExpenseDao: SaleExpenseDao = mockk(relaxed = true)

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        authPreferences = mockk(relaxed = true)
        userDao = mockk()
        googleAuthManager = mockk()
        googleDriveBackupManager = mockk(relaxed = true)
        firestoreSyncManager = mockk(relaxed = true)
        networkHelper = mockk(relaxed = true)

        viewModel = LoginViewModel(
            authPreferences,
            userDao,
            googleAuthManager,
            googleDriveBackupManager,
            firestoreSyncManager,
            itemDao,
            warehouseDao,
            supplierDao,
            lotDao,
            lotItemDao,
            lotExpenseDao,
            transactionDao,
            stockDao,
            saleItemDao,
            saleExpenseDao,
            networkHelper
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    @Test
    fun `login with valid credentials should succeed`() = runTest {
        // Given
        val email = "test@test.com"
        val password = "password"
        val user = UserProfile("user1", email, "Business", "hashed_password")
        
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        
        coEvery { userDao.getUserByCredentials(email, any()) } returns user
        coEvery { firestoreSyncManager.fetchAllUserData("user1") } returns Result.success(mockk(relaxed = true))

        // When
        viewModel.login()

        // Then
        verify { authPreferences.saveCredentials(email, any(), "user1") }
    }

    @Test
    fun `login with invalid credentials should fail`() = runTest {
        // Given
        val email = "test@test.com"
        val password = "wrong"
        
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        
        coEvery { userDao.getUserByCredentials(email, any()) } returns null

        // When
        viewModel.login()

        // Then
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.emailError != null)
    }
}
