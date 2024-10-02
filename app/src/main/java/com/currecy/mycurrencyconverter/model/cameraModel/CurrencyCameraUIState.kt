package com.currecy.mycurrencyconverter.model.cameraModel

data class CurrencyCameraUIState (
    val isLoading: Boolean = true,
    val detectedNumber: Double? = null,
    val convertedValue: Double = 0.0,
    val selectedCurrencyFrom: String = "",
    val selectedCurrencyTo: String = "",
    val conversionResult: String = ""
)