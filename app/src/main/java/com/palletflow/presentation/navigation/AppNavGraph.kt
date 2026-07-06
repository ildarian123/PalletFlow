package com.palletflow.presentation.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
            val authState by viewModel.authState.collectAsState()
            
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
            val uiState by viewModel.uiState.collectAsState()

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
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onLoginClick = {
                    viewModel.login()
                },
                onSignUpClick = {
                    navController.navigate(Screen.Register.route)
                },
                onGoogleClick = { /* Do nothing for now */ }
            )
        }
        
        composable(Screen.Register.route) {
            val viewModel: RegisterViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()

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
                onGoogleClick = { /* Do nothing for now */ },
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
                onMoveInventoryClick = { navController.navigate(Screen.MoveInventory.route) },
                onAddExpenseClick = { navController.navigate(Screen.AddExpense.route) },
                onSeeAllActivityClick = { /* Handle */ },
                onLogoutClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onInventoryItemClick = { itemId ->
                    navController.navigate(Screen.ItemDetail.createRoute(itemId))
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

        composable(Screen.ReceiveNewLot.route) {
            ReceiveLotScreen(
                onBackClick = { navController.popBackStack() },
                onSaveDraftClick = { navController.popBackStack() },
                onCompleteClick = { navController.popBackStack() },
                onAddNewSupplierClick = { /* Navigate to Add Supplier */ },
                onCreateNewItemClick = { navController.navigate(Screen.AddItem.route) }
            )
        }

        composable(Screen.NewSale.route) {
            SellScreen(
                onBackClick = { navController.popBackStack() },
                onSaveDraftClick = { navController.popBackStack() },
                onCompleteClick = { navController.popBackStack() }
            )
        }

        composable(Screen.MoveInventory.route) {
            MoveInventoryScreen(
                onBackClick = { navController.popBackStack() },
                onConfirmClick = { navController.popBackStack() }
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
