package com.currecy.mycurrencyconverter.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.currecy.mycurrencyconverter.database.CurrencyRateDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CurrencyViewModel(private val rates: CurrencyRateDao) : ViewModel() {

    private val _currencyRatesState = MutableStateFlow(ConverterUIState())
    val currencyRatesState: StateFlow<ConverterUIState> = _currencyRatesState.asStateFlow()


    fun addMoreItems() {
        _currencyRatesState.update { state ->
            if (state.numberOfItems < 6) {
                state.copy(numberOfItems = state.numberOfItems + 1)
            } else {
                state // Do nothing if 6 items are already displayed
            }
        }
    }

    fun onAmountChange(newAmount: Double, index: Int) {
        viewModelScope.launch {
            Log.d("ViewModel", "onAmountChange called: newAmount = $newAmount, index = $index")
            _currencyRatesState.update { state ->
                val updatedState = when (index) {
                    0 -> state.copy(
                        firstValue = newAmount,
                        secondValue = if (state.secondCurrency.isNotEmpty()) {
                            val converted = convertAmount(newAmount, state.firstCurrency, state.secondCurrency)
                            Log.d("ConversionLog", "Converted $newAmount from ${state.firstCurrency} to ${state.secondCurrency}: $converted")
                            converted
                        } else state.secondValue,
                        thirdValue = if (state.thirdCurrency.isNotEmpty()) {
                            val converted = convertAmount(newAmount, state.firstCurrency, state.thirdCurrency)
                            Log.d("ConversionLog", "Converted $newAmount from ${state.firstCurrency} to ${state.thirdCurrency}: $converted")
                            converted
                        } else state.thirdValue,
                        fourthValue = if (state.fourthCurrency.isNotEmpty()) {
                            val converted = convertAmount(newAmount, state.firstCurrency, state.fourthCurrency)
                            Log.d("ConversionLog", "Converted $newAmount from ${state.firstCurrency} to ${state.fourthCurrency}: $converted")
                            converted
                        } else state.fourthValue,
                        fifthValue = if (state.fifthCurrency.isNotEmpty()) {
                            val converted = convertAmount(newAmount, state.firstCurrency, state.fifthCurrency)
                            Log.d("ConversionLog", "Converted $newAmount from ${state.firstCurrency} to ${state.fifthCurrency}: $converted")
                            converted
                        } else state.fifthValue,
                        sixthValue = if (state.sixthCurrency.isNotEmpty()) {
                            val converted = convertAmount(newAmount, state.firstCurrency, state.sixthCurrency)
                            Log.d("ConversionLog", "Converted $newAmount from ${state.firstCurrency} to ${state.sixthCurrency}: $converted")
                            converted
                        } else state.sixthValue
                    )

                    1 -> state.copy(
                        secondValue = newAmount,
                        firstValue = if (state.firstCurrency.isNotEmpty()) {
                            val converted = convertAmount(newAmount, state.secondCurrency, state.firstCurrency)
                            Log.d("ConversionLog", "Converted $newAmount from ${state.secondCurrency} to ${state.firstCurrency}: $converted")
                            converted
                        } else state.firstValue,
                        thirdValue = if (state.thirdCurrency.isNotEmpty()) {
                            val converted = convertAmount(newAmount, state.secondCurrency, state.thirdCurrency)
                            Log.d("ConversionLog", "Converted $newAmount from ${state.secondCurrency} to ${state.thirdCurrency}: $converted")
                            converted
                        } else state.thirdValue,
                        fourthValue = if (state.fourthCurrency.isNotEmpty()) {
                            val converted = convertAmount(newAmount, state.secondCurrency, state.fourthCurrency)
                            Log.d("ConversionLog", "Converted $newAmount from ${state.secondCurrency} to ${state.fourthCurrency}: $converted")
                            converted
                        } else state.fourthValue,
                        fifthValue = if (state.fifthCurrency.isNotEmpty()) {
                            val converted = convertAmount(newAmount, state.secondCurrency, state.fifthCurrency)
                            Log.d("ConversionLog", "Converted $newAmount from ${state.secondCurrency} to ${state.fifthCurrency}: $converted")
                            converted
                        } else state.fifthValue,
                        sixthValue = if (state.sixthCurrency.isNotEmpty()) {
                            val converted = convertAmount(newAmount, state.secondCurrency, state.sixthCurrency)
                            Log.d("ConversionLog", "Converted $newAmount from ${state.secondCurrency} to ${state.sixthCurrency}: $converted")
                            converted
                        } else state.sixthValue
                    )
                    // Add similar logic for other indices...
                    else -> state
                }
                updatedState
            }
        }
    }

    // Function to handle currency change and trigger conversions
    fun onCurrencyChange(newCurrency: String, index: Int) {
        viewModelScope.launch {
            Log.d("ViewModel", "onCurrencyChange called: newCurrency = $newCurrency, index = $index")
            _currencyRatesState.update { state ->
                val updatedState = when (index) {
                    0 -> state.copy(
                        firstCurrency = newCurrency,
                        secondValue = if (state.secondCurrency.isNotEmpty()) convertAmount(state.firstValue, newCurrency, state.secondCurrency) else 0.0,
                        thirdValue = if (state.thirdCurrency.isNotEmpty()) convertAmount(state.firstValue, newCurrency, state.thirdCurrency) else 0.0,
                        fourthValue = if (state.fourthCurrency.isNotEmpty()) convertAmount(state.firstValue, newCurrency, state.fourthCurrency) else 0.0,
                        fifthValue = if (state.fifthCurrency.isNotEmpty()) convertAmount(state.firstValue, newCurrency, state.fifthCurrency) else 0.0,
                        sixthValue = if (state.sixthCurrency.isNotEmpty()) convertAmount(state.firstValue, newCurrency, state.sixthCurrency) else 0.0
                    )

                    1 -> state.copy(
                        secondCurrency = newCurrency,
                        firstValue = if (state.firstCurrency.isNotEmpty()) convertAmount(state.secondValue, newCurrency, state.firstCurrency) else 0.0,
                        thirdValue = if (state.thirdCurrency.isNotEmpty()) convertAmount(state.secondValue, newCurrency, state.thirdCurrency) else 0.0,
                        fourthValue = if (state.fourthCurrency.isNotEmpty()) convertAmount(state.secondValue, newCurrency, state.fourthCurrency) else 0.0,
                        fifthValue = if (state.fifthCurrency.isNotEmpty()) convertAmount(state.secondValue, newCurrency, state.fifthCurrency) else 0.0,
                        sixthValue = if (state.sixthCurrency.isNotEmpty()) convertAmount(state.secondValue, newCurrency, state.sixthCurrency) else 0.0
                    )


                    else -> state
                }
                updatedState
            }
        }
    }

    // Function to convert based on rates
    private suspend fun convertAmount(amount: Double, fromCurrency: String, toCurrency: String): Double {
        Log.d("CurrencyConversion", "Attempting to convert $amount from $fromCurrency to $toCurrency")

        // Get the rate for the 'from' currency and handle nullable cases
        val fromRate: Double? = rates.getRateForCurrency(fromCurrency)
        val toRate: Double? = rates.getRateForCurrency(toCurrency)

        Log.d("CurrencyConversion", "Rates: From Rate = $fromRate, To Rate = $toRate")

        // Check if either rate is null or 0.0
        if (fromRate == null || toRate == null) {
            Log.d("CurrencyConversion", "Could not retrieve rates. From Rate or To Rate is null.")
            return 0.0
        }

        if (fromRate == 0.0) {
            Log.d("CurrencyConversion", "From rate is 0.0, returning 0.0 to avoid division by zero.")
            return 0.0
        }

        // Perform the conversion
        val result = (amount / fromRate) * toRate
        Log.d("CurrencyConversion", "Converted $amount from $fromCurrency to $toCurrency. Result = $result")
        return result
    }




}