package com.currecy.mycurrencyconverter

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CurrencyConverter (context: Context) {

    private val sharedPrefs = context.getSharedPreferences("currency_prefs", Context.MODE_PRIVATE)

    fun convert(fromCurrency: String, toCurrency: String, amount: Double) :Double {

        val euroRatesJson = sharedPrefs.getString("euro_rates", null)

        if (euroRatesJson == null) {
            throw IllegalStateException("Currency rates not found in SharedPreferences")
        }

        val euroRates: Map<String, Double> = Gson().fromJson(euroRatesJson, object : TypeToken<Map<String, Double>>() {}.type)

        val fromRate = euroRates[fromCurrency]
        val toRate = euroRates[toCurrency]


        if (fromRate == null || toRate == null){
            throw IllegalArgumentException("Conversion rates for one or both currencies not found.")
        }

        val amountInEuro = amount / fromRate

        val convertedAmount = amountInEuro * toRate

        return convertedAmount
    }

}