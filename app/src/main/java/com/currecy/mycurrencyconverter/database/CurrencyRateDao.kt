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
    suspend fun getCount(): Int

    @Query("SELECT MIN(date) FROM currency_rates")
    suspend fun getOldestDate(): String?

    @Query("SELECT rate FROM currency_rates WHERE UPPER(currencyCode) = UPPER(:currency) ORDER BY date DESC LIMIT 1")
    suspend fun getRateForCurrency(currency: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rates: List<CurrencyRate>)

    @Query("SELECT * FROM currency_rates WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getRatesBetweenDates(startDate: String, endDate: String): List<CurrencyRate>

    @Query("SELECT * FROM currency_rates WHERE currencyCode = :currencyCode AND date = :date")
    suspend fun getRateForCurrencyOnDate(currencyCode: String, date: String): CurrencyRate?

}
