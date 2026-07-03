package com.palletflow.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        Expense::class,
        Item::class
    ],
    version = 5,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun warehouseDao(): WarehouseDao
    abstract fun supplierDao(): SupplierDao
    abstract fun lotDao(): LotDao
    abstract fun lotItemDao(): LotItemDao
    abstract fun transactionDao(): TransactionDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun itemDao(): ItemDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Initial migration history
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Migration to version 3 to fix previous schema mismatch
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `items` (
                        `id` TEXT NOT NULL, 
                        `userId` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `type` TEXT NOT NULL, 
                        `sku` TEXT, 
                        `notes` TEXT, 
                        `photoPath` TEXT, 
                        `createdAt` INTEGER NOT NULL, 
                        `updatedAt` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_items_userId` ON `items` (`userId`)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Drop old items table if exists
                db.execSQL("DROP TABLE IF EXISTS `items`")
                // Create new simplified items table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `items` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `type` TEXT NOT NULL DEFAULT 'SINGLE_PRODUCT',
                        `sku` TEXT,
                        `notes` TEXT,
                        `photoPath` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_items_userId` ON `items` (`userId`)"
                )
            }
        }
    }
}
