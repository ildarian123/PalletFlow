package com.palletflow.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object CreateProfile : Screen("create_profile")
    object Onboarding : Screen("onboarding")
    
    object Dashboard : Screen("dashboard")
    object Inventory : Screen("inventory")
    object Transactions : Screen("transactions")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
    
    object WarehouseList : Screen("warehouse_list")
    object WarehouseDetail : Screen("warehouse_detail/{id}") {
        fun createRoute(id: String) = "warehouse_detail/$id"
    }
    object NewWarehouse : Screen("new_warehouse")
    object EditWarehouse : Screen("edit_warehouse/{id}") {
        fun createRoute(id: String) = "edit_warehouse/$id"
    }
    
    object SupplierList : Screen("supplier_list")
    object SupplierDetail : Screen("supplier_detail/{id}") {
        fun createRoute(id: Int) = "supplier_detail/$id"
    }
    object NewSupplier : Screen("new_supplier")
    
    object LotList : Screen("lot_list")
    object LotDetail : Screen("lot_detail/{id}") {
        fun createRoute(id: Int) = "lot_detail/$id"
    }
    object ReceiveNewLot : Screen("receive_new_lot")
    
    object ItemDetail : Screen("item_detail/{itemId}") {
        fun createRoute(itemId: String) = "item_detail/$itemId"
    }
    object EditItem : Screen("edit_item/{itemId}") {
        fun createRoute(itemId: String) = "edit_item/$itemId"
    }
    object AddItem : Screen("add_item")
    object AddExpense : Screen("add_expense")
    
    object NewSale : Screen("new_sale")
    object SellItem : Screen("sell_item/{id}") {
        fun createRoute(id: Int) = "sell_item/$id"
    }
    object MoveInventory : Screen("move_inventory")
    object MoveInventoryDetail : Screen("move_inventory_detail/{id}") {
        fun createRoute(id: Int) = "move_inventory_detail/$id"
    }
    
    object SalesReport : Screen("sales_report")
    object ProfitByLot : Screen("profit_by_lot")
    object StockLevels : Screen("stock_levels")
    object TopItems : Screen("top_items")
    
    object ExportQuickBooks : Screen("export_quickbooks")
    object ExportEbay : Screen("export_ebay")
}