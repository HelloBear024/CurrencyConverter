package com.currecy.mycurrencyconverter

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class CurrencyApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // Override the workManagerConfiguration property
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        Log.d("CurrencyApp", "Application onCreate called")
        scheduleCurrencyWorker()
    }

    private fun scheduleCurrencyWorker() {
        val workManager = WorkManager.getInstance(applicationContext)

        // Define constraints for the worker (e.g., network availability)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Create a PeriodicWorkRequest to run daily
        val currencyWorkRequest = PeriodicWorkRequestBuilder<CurrencyWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        // Enqueue the work uniquely to avoid duplicate workers
        workManager.enqueueUniquePeriodicWork(
            "CurrencyWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            currencyWorkRequest
        )

        Log.d("CurrencyApp", "CurrencyWorker enqueued")

    }


}