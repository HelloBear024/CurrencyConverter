package com.currecy.mycurrencyconverter.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [CurrencyRate::class, UserCurrencyPreference::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun currencyRateDao(): CurrencyRateDao
    abstract fun userCurrencyPreferenceDao(): UserCurrencyPreferenceDao

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
                )
                    .addMigrations(MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migrate currency_rates table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS currency_rates_new (
                        currencyCode TEXT NOT NULL,
                        rate REAL NOT NULL,
                        date TEXT NOT NULL,
                        PRIMARY KEY(currencyCode, date)
                    )
                """.trimIndent()
                )

                database.execSQL(
                    """
                    INSERT INTO currency_rates_new (currencyCode, rate, date)
                    SELECT currencyCode, rate, date FROM currency_rates
                """.trimIndent()
                )

                database.execSQL("DROP TABLE currency_rates")
                database.execSQL("ALTER TABLE currency_rates_new RENAME TO currency_rates")

                // Migrate user_currency_preferences table by adding new columns with default values
                database.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS user_currency_preferences (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                firstCurrencyCode TEXT NOT NULL DEFAULT '',
                secondCurrencyCode TEXT NOT NULL DEFAULT ''
            )
        """.trimIndent()
                )
            }
        }
    }
}
