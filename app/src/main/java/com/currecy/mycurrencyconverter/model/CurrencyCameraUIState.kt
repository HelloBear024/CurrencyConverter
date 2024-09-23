package com.currecy.mycurrencyconverter.model

data class CurrencyCameraUIState (
    val detectedNumber: Double = 0.0,
    val selectedCurrencyFrom: String = "",
    val selectedCurrencyTo: String = "",
    val convertedValue: Double = 0.0
)