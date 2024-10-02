package com.currecy.mycurrencyconverter.database.preferencess.home

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_currency_conversion")
 data class HomePageConversionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val index: Int,
    val selectedCurrency: String
)
