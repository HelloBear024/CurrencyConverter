package com.currecy.mycurrencyconverter.model

data class ConverterUIState(
    val firstValue: Double = 0.0,
    val firstCurrency: String = "afn",

    val secondValue: Double = 0.0,
    val secondCurrency: String = "afn",

    val thirdValue: Double = 0.0,
    val thirdCurrency: String = "afn",

    val fourthValue: Double = 0.0,
    val fourthCurrency: String = "",

    val fifthValue: Double = 0.0,
    val fifthCurrency: String = "",

    val sixthValue: Double = 0.0,
    val sixthCurrency: String = "",

    val seventhValue: Int = 0,
    val seventhCurrency: String = "",

    val eightValue: Int = 0,
    val eightCurrency: String = "",

    val numberOfItems: Int = 2

) {
}