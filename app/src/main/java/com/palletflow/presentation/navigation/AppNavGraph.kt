package com.palletflow.presentation.navigation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.palletflow.presentation.screens.auth.LoginScreen
import com.palletflow.presentation.screens.auth.LoginViewModel
import com.palletflow.presentation.screens.auth.RegisterScreen
import com.palletflow.presentation.screens.auth.RegisterViewModel
import com.palletflow.presentation.screens.auth.CreateProfileScreen
import com.palletflow.presentation.screens.auth.SplashScreen
import com.palletflow.presentation.screens.auth.SplashViewModel
import com.palletflow.presentation.screens.dashboard.DashboardScreen
import com.palletflow.presentation.screens.main.MainScreen
import com.palletflow.presentation.screens.inventory.InventoryScreen
import com.palletflow.presentation.screens.inventory.ItemDetailScreen
import com.palletflow.presentation.screens.inventory.EditItemScreen
import com.palletflow.presentation.screens.inventory.AddItemScreen
import com.palletflow.presentation.screens.warehouse.WarehouseListScreen
import com.palletflow.presentation.screens.warehouse.AddWarehouseScreen
import com.palletflow.presentation.screens.warehouse.WarehouseDetailScreen
import com.palletflow.presentation.screens.warehouse.EditWarehouseScreen
import com.palletflow.presentation.screens.supplier.NewSupplierScreen
import com.palletflow.presentation.screens.sales.SalesScreen
import com.palletflow.presentation.screens.lot.ReceiveLotScreen
import com.palletflow.presentation.screens.sell.SellScreen
import com.palletflow.presentation.screens.move.MoveInventoryScreen
import com.palletflow.presentation.screens.transactions.AddExpenseScreen
import com.palletflow.presentation.navigation.BottomNavTab

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            val viewModel: SplashViewModel = hiltViewModel()
            val authState by viewModel.authState.collectAsStateWithLifecycle()

            LaunchedEffect(authState) {
                when (authState) {
                    SplashViewModel.AuthState.Authenticated -> {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                    SplashViewModel.AuthState.Unauthenticated -> {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                    else -> {}
                }
            }
            
            SplashScreen()
        }

        composable(Screen.Login.route) {
            val viewModel: LoginViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val context = androidx.compose.ui.platform.LocalContext.current
            val googleLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                android.util.Log.d("AppNavGraph", "Google Sign-In result received: ${result.resultCode}")
                if (result.resultCode == Activity.RESULT_OK) {
                    android.widget.Toast.makeText(context, "Sign-In Success! Processing...", android.widget.Toast.LENGTH_SHORT).show()
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    viewModel.handleGoogleSignIn(task)
                } else {
                    android.util.Log.e("AppNavGraph", "Google Sign-In failed with code: ${result.resultCode}")
                    android.widget.Toast.makeText(context, "Sign-In Failed or Cancelled (Code: ${result.resultCode})", android.widget.Toast.LENGTH_LONG).show()
                }
            }

            LaunchedEffect(Unit) {
                viewModel.loginEvent.collect { success ->
                    if (success) {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            }

            LoginScreen(
                emailValue = uiState.email,
                emailError = uiState.emailError,
                passwordValue = uiState.password,
                passwordError = uiState.passwordError,
                isLoading = uiState.isLoading,
                errorMessage = uiState.error,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onLoginClick = {
                    viewModel.login()
                },
                onSignUpClick = {
                    navController.navigate(Screen.Register.route)
                },
                onGoogleClick = {
                    android.util.Log.d("AppNavGraph", "onGoogleClick triggered")
                    try {
                        val intent = viewModel.getGoogleSignInIntent()
                        android.util.Log.d("AppNavGraph", "Launching Google Sign-In intent")
                        googleLauncher.launch(intent)
                    } catch (e: Exception) {
                        android.util.Log.e("AppNavGraph", "Failed to launch Google Sign-In", e)
                        // Show a placeholder error for the user
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            val viewModel: RegisterViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            val googleLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    // You might want a handleGoogleSignUp or reuse handleGoogleSignIn if it handles creation
                    // For now, let's just focus on Login as requested, but I'll add the button if needed.
                }
            }

            LaunchedEffect(Unit) {
                viewModel.registrationEvent.collect { success ->
                    if (success) {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                }
            }

            RegisterScreen(
                state = state,
                onBusinessNameChange = viewModel::onBusinessNameChange,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                onBackClick = {
                    navController.navigateUp()
                },
                onRegisterClick = {
                    viewModel.register()
                },
                onGoogleClick = { /* Handle if needed */ },
                onSignInClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.CreateProfile.route) {
            CreateProfileScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.CreateProfile.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Dashboard.route) {
            var currentTab by rememberSaveable { mutableStateOf(BottomNavTab.Dashboard) }
            
            DashboardScreen(
                onReceiveLotClick = { navController.navigate(Screen.ReceiveNewLot.route) },
                onSellItemClick = { navController.navigate(Screen.NewSale.route) },
                onAddSaleClickWithId = { saleId ->
                    navController.navigate(Screen.NewSale.createRoute(saleId))
                },
                onMoveInventoryClick = { navController.navigate(Screen.MoveInventory.route) },
                onAddTransferClickWithId = { transferId ->
                    navController.navigate(Screen.MoveInventory.createRoute(transferId))
                },
                onSeeAllActivityClick = { /* Do nothing */ },
                onLogoutClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onInventoryItemClick = { itemId ->
                    navController.navigate(Screen.ItemDetail.createRoute(itemId))
                },
                onEditLotClick = { lotId ->
                    navController.navigate(Screen.ReceiveNewLot.createRoute(lotId))
                },
                onAddItemClick = {
                    navController.navigate(Screen.AddItem.route)
                },
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                navController = navController
            )
        }

        composable(Screen.WarehouseList.route) {
            WarehouseListScreen(
                onBackClick = { navController.popBackStack() },
                onWarehouseClick = { warehouseId ->
                    navController.navigate(Screen.WarehouseDetail.createRoute(warehouseId))
                },
                onAddWarehouseClick = {
                    navController.navigate(Screen.NewWarehouse.route)
                }
            )
        }

        composable(
            route = Screen.ItemDetail.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType }
            )
        ) {
            ItemDetailScreen(
                onBackClick = { navController.popBackStack() },
                onEditClick = { itemId ->
                    navController.navigate(Screen.EditItem.createRoute(itemId))
                },
                onSellClick = { itemId ->
                    // Handle sell
                },
                onLotClick = { /* Handle lot click */ }
            )
        }

        composable(
            route = Screen.EditItem.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType }
            )
        ) {
            EditItemScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AddItem.route) {
            AddItemScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { 
                    // Handle save
                    navController.popBackStack()
                },
                onScanClick = { /* Handle scan */ }
            )
        }

        composable(Screen.NewWarehouse.route) {
            AddWarehouseScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = {
                    // Handle save
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.NewSupplier.route) {
            NewSupplierScreen(
                onBackClick = { navController.popBackStack() },
                onSupplierSaved = { navController.popBackStack() }
            )
        }

        composable(Screen.WarehouseDetail.route) {
            WarehouseDetailScreen(
                onBackClick = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate(Screen.EditWarehouse.createRoute(id))
                },
                onAddLotClick = { /* Handle add lot */ },
                onMoveItemsClick = { /* Handle move items */ }
            )
        }

        composable(Screen.EditWarehouse.route) {
            EditWarehouseScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Transactions.route) {
            SalesScreen(
                onAddLotClick = { navController.navigate(Screen.ReceiveNewLot.route) },
                onEditLotClick = { lotId -> 
                    navController.navigate(Screen.ReceiveNewLot.createRoute(lotId))
                },
                onAddSaleClick = { navController.navigate(Screen.NewSale.createRoute()) },
                onAddSaleClickWithId = { saleId ->
                    navController.navigate(Screen.NewSale.createRoute(saleId))
                },
                onAddTransferClick = { navController.navigate(Screen.MoveInventory.route) },
                onAddTransferClickWithId = { transferId ->
                    navController.navigate(Screen.MoveInventory.createRoute(transferId))
                }
            )
        }

        composable(
            route = Screen.ReceiveNewLot.route,
            arguments = listOf(
                navArgument("lotId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            ReceiveLotScreen(
                onBackClick = { navController.popBackStack() },
                onCompleteClick = {
                    // Try to pop back to Transactions, if not found just pop back
                    if (!navController.popBackStack(Screen.Transactions.route, inclusive = false)) {
                        navController.popBackStack()
                    }
                },
                onAddNewSupplierClick = { navController.navigate(Screen.NewSupplier.route) },
                onCreateNewItemClick = { navController.navigate(Screen.AddItem.route) }
            )
        }

        composable(
            route = Screen.NewSale.route,
            arguments = listOf(
                navArgument("saleId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            SellScreen(
                onBackClick = { navController.popBackStack() },
                onCompleteClick = {
                    if (!navController.popBackStack(Screen.Transactions.route, inclusive = false)) {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(
            route = Screen.MoveInventory.route,
            arguments = listOf(
                navArgument("transferId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            MoveInventoryScreen(
                onBackClick = { navController.popBackStack() },
                onConfirmClick = {
                    // Try to pop back to Transactions tab, if not just pop back
                    if (!navController.popBackStack(Screen.Transactions.route, inclusive = false)) {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(Screen.AddExpense.route) {
            AddExpenseScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() },
                onAttachReceiptClick = { /* Handle receipt */ }
            )
        }
    }
}
