package com.currecy.mycurrencyconverter.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.currecy.mycurrencyconverter.database.CurrencyRateDao

class CurrencyViewModelFactory(
    private val currencyDao: CurrencyRateDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CameraViewModel::class.java) -> {
                CameraViewModel(currencyDao) as T
            }

            modelClass.isAssignableFrom(CurrencyViewModel::class.java) -> {
                CurrencyViewModel(currencyDao) as T
            }

            else ->
                throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}



