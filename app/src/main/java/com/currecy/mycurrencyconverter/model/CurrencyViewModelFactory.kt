package com.currecy.mycurrencyconverter.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.currecy.mycurrencyconverter.database.CurrencyRateDao

class CurrencyViewModelFactory(private val currencyDao: CurrencyRateDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CurrencyViewModel::class.java)) {
            return CurrencyViewModel(currencyDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}