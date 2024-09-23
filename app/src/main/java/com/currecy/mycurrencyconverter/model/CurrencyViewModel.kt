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
                // Create a new list of converted values
                val updatedValues = state.values.mapIndexed { i, currentValue ->
                    if (i != index && state.currencies[i].isNotEmpty()) {
                        // Convert the value from the selected currency at the index to all other currencies
                        val converted = convertAmount(
                            amount = newAmount,
                            fromCurrency = state.currencies[index],
                            toCurrency = state.currencies[i]
                        )
                        val formattedValue = formatToTwoDecimals(converted) // Format to two decimals
                        Log.d("ConversionLog", "Converted $newAmount from ${state.currencies[index]} to ${state.currencies[i]}: $formattedValue")
                        formattedValue
                    } else if (i == index) {
                        formatToTwoDecimals(newAmount) // Update the amount at the selected index and format it
                    } else {
                        currentValue
                    }
                }

                // Update the state with the new converted values
                state.copy(values = updatedValues)
            }
        }
    }

    fun onCurrencyChange(newCurrency: String, index: Int) {
        viewModelScope.launch {
            Log.d("ViewModel", "onCurrencyChange called: newCurrency = $newCurrency, index = $index")

            _currencyRatesState.update { state ->

                // Update only the currency for the selected index
                val updatedCurrencies = state.currencies.toMutableList().apply {
                    this[index] = newCurrency
                }

                // Recalculate the value for the specific index if there's an amount
                val updatedValues = state.values.toMutableList().apply {
                    if (state.values[index] != 0.0) {
                        this[index] = formatToTwoDecimals(
                            convertAmount(
                                amount = state.values[index],
                                fromCurrency = state.currencies[index],  // previous currency
                                toCurrency = newCurrency  // new currency
                            )
                        )
                    }
                }

                // Update the state with new currencies and updated value only for the selected index
                state.copy(values = updatedValues, currencies = updatedCurrencies)
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


    private fun formatToTwoDecimals(value: Double): Double {
        return "%.2f".format(value).toDouble()
    }

}