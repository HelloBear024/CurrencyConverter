package com.currecy.mycurrencyconverter.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.currecy.mycurrencyconverter.database.preferencess.camera.CameraPagePreferencesDao
import com.currecy.mycurrencyconverter.database.preferencess.camera.CameraPagePreferencesEntity
import com.currecy.mycurrencyconverter.database.preferencess.home.HomePageConversionEntity
import com.currecy.mycurrencyconverter.database.preferencess.home.HomePageCurrencyConversionDao

@Database(entities = [CurrencyRate::class, UserCurrencyPreference::class,
    HomePageConversionEntity::class, CameraPagePreferencesEntity::class], version = 5, exportSchema = false)
    abstract class AppDatabase : RoomDatabase() {
    abstract fun currencyRateDao(): CurrencyRateDao
    abstract fun userCurrencyPreferenceDao(): UserCurrencyPreferenceDao
    abstract fun homePageCurrencyConversionDao(): HomePageCurrencyConversionDao
    abstract fun cameraPagePreferenceDao(): CameraPagePreferencesDao


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
                    .addMigrations(MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
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

                // **Add Migration for home_currency_conversion Table**
                // Drop the existing table if it exists (to handle potential previous faulty migrations)
                database.execSQL("DROP TABLE IF EXISTS home_currency_conversion")

                // Create the home_currency_conversion table with the correct schema
                database.execSQL(
                    """
                    CREATE TABLE home_currency_conversion (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `index` INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        selectedCurrency TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS camera_page_preference (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        firstCurrency TEXT NOT NULL DEFAULT '',
                        secondCurrency TEXT NOT NULL DEFAULT ''
                    )
                    """.trimIndent()
                )


            }
        }
    }
}
