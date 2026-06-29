package com.palletflow.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.palletflow.data.local.dao.*
import com.palletflow.data.local.entity.*

@Database(
    entities = [
        UserProfile::class,
        Warehouse::class,
        Supplier::class,
        Lot::class,
        LotItem::class,
        Transaction::class,
        Expense::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun warehouseDao(): WarehouseDao
    abstract fun supplierDao(): SupplierDao
    abstract fun lotDao(): LotDao
    abstract fun lotItemDao(): LotItemDao
    abstract fun transactionDao(): TransactionDao
    abstract fun expenseDao(): ExpenseDao
}