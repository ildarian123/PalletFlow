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
        Item::class,
        Stock::class,
        LotExpense::class
    ],
    version = 7,
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
    abstract fun stockDao(): StockDao
    abstract fun lotExpenseDao(): LotExpenseDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {}
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {}
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
                db.execSQL("DROP TABLE IF EXISTS `items`")
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
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_items_userId` ON `items` (`userId`)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `transactions`")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `transactions` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `itemId` TEXT,
                        `lotId` TEXT,
                        `quantity` INTEGER NOT NULL,
                        `amount` REAL NOT NULL,
                        `warehouseFromId` TEXT,
                        `warehouseToId` TEXT,
                        `platform` TEXT,
                        `notes` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_userId` ON `transactions` (`userId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_itemId` ON `transactions` (`itemId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_lotId` ON `transactions` (`lotId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_type` ON `transactions` (`type`)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Update Suppliers table from stub
                db.execSQL("DROP TABLE IF EXISTS `suppliers`")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `suppliers` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `contactName` TEXT,
                        `email` TEXT,
                        `phone` TEXT,
                        `address` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """)

                // Update Lots table from stub
                db.execSQL("DROP TABLE IF EXISTS `lots`")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `lots` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `lotNumber` TEXT NOT NULL,
                        `supplierId` TEXT NOT NULL,
                        `warehouseId` TEXT NOT NULL,
                        `purchasePrice` REAL NOT NULL,
                        `status` TEXT NOT NULL,
                        `notes` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_lots_userId` ON `lots` (`userId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_lots_supplierId` ON `lots` (`supplierId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_lots_warehouseId` ON `lots` (`warehouseId`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_lots_lotNumber` ON `lots` (`lotNumber`)")

                // Add Stock table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `stocks` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `itemId` TEXT NOT NULL,
                        `warehouseId` TEXT NOT NULL,
                        `quantity` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stocks_userId` ON `stocks` (`userId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stocks_itemId` ON `stocks` (`itemId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stocks_warehouseId` ON `stocks` (`warehouseId`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_stocks_userId_itemId_warehouseId` ON `stocks` (`userId`, `itemId`, `warehouseId`)")

                // Add LotExpense table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `lot_expenses` (
                        `id` TEXT NOT NULL,
                        `lotId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`lotId`) REFERENCES `lots`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_lot_expenses_lotId` ON `lot_expenses` (`lotId`)")
            }
        }
    }
}
