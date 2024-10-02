package com.currecy.mycurrencyconverter.model.searchChart

data class ChartCurrencyState(
    val id: Int,
    val sourceCurrency:String,
    val targetCurrency: String,
    val currentRate: Double = 0.0,
    val percentageChange: Double = 0.0
)