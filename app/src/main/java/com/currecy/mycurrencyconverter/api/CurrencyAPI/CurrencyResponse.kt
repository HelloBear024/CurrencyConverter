package com.currecy.mycurrencyconverter.api.CurrencyAPI

data class CurrencyResponse(
    val date: String,
    val eur: Map<String, Double>
)
