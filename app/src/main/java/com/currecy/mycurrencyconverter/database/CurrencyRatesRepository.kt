package com.currecy.mycurrencyconverter.database

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

}