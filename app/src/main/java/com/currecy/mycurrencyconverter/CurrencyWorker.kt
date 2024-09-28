package com.currecy.mycurrencyconverter

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.currecy.mycurrencyconverter.data.CurrencyOptionsData
import com.currecy.mycurrencyconverter.database.AppDatabase
import com.currecy.mycurrencyconverter.database.CurrencyRate
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@HiltWorker
class CurrencyWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams)
{
    private val currencyDao = AppDatabase.getDatabase(context).currencyRateDao()

    override suspend fun doWork(): Result {
        Log.d("CurrencyWorker", "Worker started")
         return try {
            val today = getCurrentDate()
             Log.d("CurrencyWorker", "Fetching data for $today")
            val isFirstRun = isFirstRun()

            if (isFirstRun) {
                Log.d("CurrencyWorker", "First run detected, fetching 30 days of data")
                for (i in 0..29) {
                    val date = getOldDate(i)
                    fetchAndStoreDataForDate(date)
                }
                markFirstRunComplete()
                Log.d("Currency Worker:  ", "is first run called ")
            }
             fetchAndStoreDataForDate(today)
             deleteOldDataIfNecessary()

             Result.success()

        } catch (e: Exception) {
            Log.e("CurrencyWorker", "Error fetching currency data: ${e.message}")
            Result.retry()
        }
    }

    private suspend fun fetchAndStoreDataForDate(date: String) {
        val url = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@$date/v1/currencies/eur.json"
        Log.d("CurrencyWorker", "Fetching data from URL: $url")

        val response = try {
            RetrofitInstance.api.getCurrencyRates(url)
        } catch (e: Exception) {
            Log.e("CurrencyWorker", "Network request failed: ${e.message}")
            return
        }

        if (response.eur != null) {

            val selectedCurrencyCodes = CurrencyOptionsData.options.map { it.second.lowercase(Locale.getDefault()) }

            response.eur.filter { (currencyCode, _) ->
                selectedCurrencyCodes.contains(currencyCode.lowercase(Locale.getDefault()))  // Filter based on the allowed currency codes
            }.forEach { (currencyCode, rate) ->
                val currencyRate = CurrencyRate(currencyCode = currencyCode, rate = rate, date = date)
                currencyDao.insert(currencyRate)
                Log.d("CurrencyWorker", "Inserting currency rate: $currencyCode -> $rate on $date")
            }
        } else {
            Log.e("CurrencyWorker", "No data fetched for EUR on $date")
        }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private suspend fun deleteOldDataIfNecessary() {
        val count = currencyDao.getCount()
        Log.d("CurrencyWorker", "Current entry count: $count")
        if (count > 30) {
            val oldestDate = currencyDao.getOldestDate()
            if (oldestDate != null) {
                currencyDao.deleteOldData(oldestDate)
                Log.d("CurrencyWorker", "Deleted oldest data for date: $oldestDate")
            }
        }
    }

    private fun getOldDate(daysAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    private fun isDigitalCurrency(currencyCode: String): Boolean {
        val digitalCurrencies = listOf("btc", "eth", "ltc", "doge", "xrp", "usdt", "ada") // Add more as needed
        return digitalCurrencies.contains(currencyCode.lowercase(Locale.getDefault()))
    }

    private fun isFirstRun(): Boolean {
        val sharedPrefs = applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean("is_first_run", true)
    }

    private fun markFirstRunComplete() {
        val sharedPrefs = applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("is_first_run", false).apply()
    }
}