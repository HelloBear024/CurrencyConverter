package com.currecy.mycurrencyconverter

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson

class CurrencyWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams)
{

    override suspend fun doWork(): Result {

        return try {
            // Fetch the currency rates for Euro
            val response = RetrofitInstance.api.getCurrencyRates("eur")

            Log.d("CurrencyWorker", "API Response: ${response.eur}")  // Log the API response

            if (response.eur != null) {
                // Store the rates in a shared storage or database (for reuse)
                // For example, store in SharedPreferences or Room
                val sharedPrefs = applicationContext.getSharedPreferences("currency_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("euro_rates", Gson().toJson(response.eur)).apply()
                Log.d("CurrencyWorker", "Stored rates in SharedPreferences: ${response.eur}")

                Result.success()
            } else {
                Log.e("CurrencyWorker", "No data fetched for EUR")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e("CurrencyWorker", "Error fetching EUR rates: ${e.message}")
            Result.retry() // Retry if there's an error
        }
    }
}