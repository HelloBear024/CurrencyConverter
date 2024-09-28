package com.currecy.mycurrencyconverter.database
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_currency_preferences")
data class UserCurrencyPreference(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firstCurrencyCode: String,
    val secondCurrencyCode: String,

    )
