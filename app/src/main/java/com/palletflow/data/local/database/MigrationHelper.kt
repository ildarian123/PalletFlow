package com.palletflow.data.local.database

object MigrationHelper {

    /*
    HOW TO ADD A NEW MIGRATION:

    Step 1: Make your change to the Entity data class

    Step 2: Increment version in @Database annotation
        version = 2 → version = 3

    Step 3: Add a new Migration object in AppDatabase companion object:
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // your SQL here
            }
        }

    Step 4: Register it in DatabaseModule:
        .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)

    -----------------------------------------------------------------------
    COMMON SQL MIGRATION PATTERNS:

    ADD a new column (nullable or with default value only):
        database.execSQL(
            "ALTER TABLE table_name ADD COLUMN column_name TEXT DEFAULT NULL"
        )
        database.execSQL(
            "ALTER TABLE table_name ADD COLUMN is_active INTEGER NOT NULL DEFAULT 1"
        )
        NOTE: INTEGER = Boolean in Room (1=true, 0=false)
        NOTE: TEXT = String, REAL = Double/Float, INTEGER = Int/Long/Boolean

    REMOVE a column or RENAME a column (SQLite does not support these directly):
    Use table recreation strategy:
        // 1. Create new table with updated schema
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `table_name_new` (
                `id` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
        """)
        // 2. Copy data from old table (only columns that exist in both)
        database.execSQL("""
            INSERT INTO table_name_new (id, userId, name)
            SELECT id, userId, name FROM table_name
        """)
        // 3. Drop old table
        database.execSQL("DROP TABLE table_name")
        // 4. Rename new table to original name
        database.execSQL("ALTER TABLE table_name_new RENAME TO table_name")

    ADD a new table:
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `new_table` (
                `id` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE
            )
        """)
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_new_table_userId` ON `new_table` (`userId`)"
        )

    DROP a table:
        database.execSQL("DROP TABLE IF EXISTS table_name")

    -----------------------------------------------------------------------
    MIGRATION HISTORY:

    Version 1 → 2:
        Initial schema establish and formal migration strategy implementation.
        Date: 2025-05-22

    // Add new entries here as you make changes
    */
}
