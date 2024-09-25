package com.currecy.mycurrencyconverter

data class CurrencyResponse(
    val date: String,
    val eur: Map<String, Double>
)
