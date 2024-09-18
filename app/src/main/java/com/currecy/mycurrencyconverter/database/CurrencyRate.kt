package com.currecy.mycurrencyconverter.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currency_rates")
data class CurrencyRate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val currencyCode: String,
    val rate: Double,
    val date: String
)
