package com.currecy.mycurrencyconverter.model

data class ConverterUIState(
    val values: List<Double> = List(6) { 0.0 },
    val currencies: List<String> = List(6) { "eur" },

    val numberOfItems: Int = 2

) {
}