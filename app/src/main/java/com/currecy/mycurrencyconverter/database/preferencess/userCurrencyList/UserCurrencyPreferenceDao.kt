package com.currecy.mycurrencyconverter.database.preferencess.userCurrencyList

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserCurrencyPreferenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userCurrencyPreference: UserCurrencyPreference)

    @Query("""
        SELECT * FROM user_currency_preferences 
        WHERE firstCurrencyCode = :currency1 AND secondCurrencyCode = :currency2
    """)
    suspend fun getCurrencyPreference(currency1: String, currency2: String): UserCurrencyPreference?

    @Query("SELECT * FROM user_currency_preferences")
    fun getAllCurrencyPreferences(): Flow<List<UserCurrencyPreference>>

    @Query("SELECT * FROM user_currency_preferences WHERE id = :id")
    fun getCurrencyPreferenceById(id: Int): Flow<UserCurrencyPreference?>


    @Query("DELETE FROM user_currency_preferences WHERE id = :id")
    suspend fun deleteById(id: Int)
}