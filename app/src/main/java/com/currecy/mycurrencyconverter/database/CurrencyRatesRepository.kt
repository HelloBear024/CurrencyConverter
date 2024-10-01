package com.currecy.mycurrencyconverter.database

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class CurrencyRatesRepository @Inject constructor(
    private val currencyRateDao: CurrencyRateDao
) {
    suspend fun getRatesBetweenDates(startDate: String, endDate: String): List<CurrencyRate> =
        currencyRateDao.getRatesBetweenDates(startDate, endDate)

    suspend fun getRateForCurrencyOnDate(currencyCode: String, date: String): CurrencyRate? =
        currencyRateDao.getRateForCurrencyOnDate(currencyCode, date)

    suspend fun insertAllRates(rates: List<CurrencyRate>) = currencyRateDao.insertAll(rates)

    suspend fun getRateForCurrency(currencyCode: String): Double? {
        return currencyRateDao.getRateForCurrency(currencyCode)
    }

    suspend fun getPreviousRate(currencyCode: String): Double {
        val yesterday = getPreviousDate()

        return currencyRateDao.getRateForCurrencyOnDate(currencyCode, yesterday)?.rate ?: 0.0

    }

    suspend fun getCurrentRate(currencyCode: String): Double {
        val today = getCurrentDate()
        return currencyRateDao.getRateForCurrencyOnDate(currencyCode, today)?.rate ?: 0.0
    }


    private fun getPreviousDate(): String {
        // Function to get the previous day's date in the format your database requires
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    private fun getCurrentDate(): String {
        // Function to get the current date in the format your database requires
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }



    suspend fun getDifferenceBetweenTwoRatesFromYesterdayToToday(
        fromCurrencyCode: String, toCurrencyCode: String) {

    }

}