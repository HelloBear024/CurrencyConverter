package com.currecy.mycurrencyconverter.model.homeModel

data class ConverterUIState(
    val valueTexts: List<String> = List(6) { "" },
    val currencies: List<String> = List(6) { "eur" },
    val numberOfItems: Int = 2

) {
}