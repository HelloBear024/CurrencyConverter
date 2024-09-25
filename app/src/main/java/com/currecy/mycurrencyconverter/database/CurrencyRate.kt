package com.currecy.mycurrencyconverter.database

import androidx.room.Entity

@Entity(tableName = "currency_rates", primaryKeys = ["currencyCode", "date"])
data class CurrencyRate(
    val currencyCode: String,
    val rate: Double,
    val date: String
)
