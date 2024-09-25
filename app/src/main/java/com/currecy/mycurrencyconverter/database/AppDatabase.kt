package com.currecy.mycurrencyconverter.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [CurrencyRate::class], version = 2, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun currencyRateDao(): CurrencyRateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            Log.d("AppDatabase", "getDatabase called")
            return INSTANCE ?: synchronized(this) {
                Log.d("AppDatabase", "Creating new database instance")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "currency_database"
                ).addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create a new table with the new schema
                database.execSQL("""
            CREATE TABLE IF NOT EXISTS currency_rates_new (
                currencyCode TEXT NOT NULL,
                rate REAL NOT NULL,
                date TEXT NOT NULL,
                PRIMARY KEY(currencyCode, date)
            )
        """.trimIndent())

                // Copy data from the old table to the new table
                database.execSQL("""
            INSERT INTO currency_rates_new (currencyCode, rate, date)
            SELECT currencyCode, rate, date FROM currency_rates
        """.trimIndent())

                // Drop the old table
                database.execSQL("DROP TABLE currency_rates")

                // Rename the new table to the old table name
                database.execSQL("ALTER TABLE currency_rates_new RENAME TO currency_rates")
            }
        }
    }
}