package com.currecy.mycurrencyconverter

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit

class CurrencyViewModel(application: Application) : AndroidViewModel(application) {

    private val _currencyRates = MutableStateFlow<Map<String, Double>>(emptyMap())
    val currencyRates: StateFlow<Map<String, Double>> = _currencyRates

    init {
        loadStoredRates()
        scheduleCurrencyWork()
    }

    private fun loadStoredRates() {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("currency_prefs", Context.MODE_PRIVATE)
        val euroRatesJson = sharedPrefs.getString("euro_rates", null)

        if (euroRatesJson != null) {
            Log.d("CurrencyRates", "Stored JSON in SharedPreferences: $euroRatesJson")
            val rates: Map<String, Double> = Gson().fromJson(euroRatesJson, object : TypeToken<Map<String, Double>>() {}.type)
            _currencyRates.value = rates
        } else {
            Log.e("CurrencyViewModel", "No stored Euro rates found")
        }
    }

    private fun scheduleCurrencyWork() {
        val workManager = WorkManager.getInstance(getApplication<Application>().applicationContext)

        // Create the periodic work request (runs every 3 hours)
        val periodicRequest = PeriodicWorkRequestBuilder<CurrencyWorker>(3, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()


        val oneTimeRequest = OneTimeWorkRequestBuilder<CurrencyWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()


        workManager.enqueue(oneTimeRequest)


        workManager.enqueueUniquePeriodicWork(
            "fetch_currency_work",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )

        Log.d("CurrencyViewModel", "WorkManager scheduled to fetch currency rates")
    }

}