package com.currecy.mycurrencyconverter.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CurrencyRateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(currencyRate: CurrencyRate)

    @Query("DELETE FROM currency_rates WHERE date < :oldDate")
    suspend fun deleteOldData(oldDate: String)

    @Query("SELECT * FROM currency_rates WHERE date >= :startDate")
    fun getRecentRates(startDate: String): LiveData<List<CurrencyRate>>

    @Query("SELECT COUNT(*) FROM currency_rates")
    suspend fun getCount(): Int  // This is the method to get the count of entries

    @Query("SELECT MIN(date) FROM currency_rates")
    suspend fun getOldestDate(): String?
//    @Query("SELECT rate FROM currency_rates WHERE currencyCode = :currencyCode LIMIT 1")
//    suspend fun getRateForCurrency(currencyCode: String): Double?

    @Query("SELECT rate FROM currency_rates WHERE LOWER(currencyCode) = LOWER(:currency) LIMIT 1")
    suspend fun getRateForCurrency(currency: String): Double?

}
