package com.currecy.mycurrencyconverter.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.currecy.mycurrencyconverter.database.CurrencyRatesRepository
import com.currecy.mycurrencyconverter.database.preferencess.home.HomePageConversionEntity
import com.currecy.mycurrencyconverter.database.preferencess.home.HomePageConversionPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val rates: CurrencyRatesRepository,
    private val repository: HomePageConversionPreferencesRepository
            ) : ViewModel() {

    private val _currencyRatesState = MutableStateFlow(ConverterUIState())
    val currencyRatesState: StateFlow<ConverterUIState> = _currencyRatesState.asStateFlow()

    init {
        viewModelScope.launch {
            // Check if data is ready before loading
                loadStateFromDatabase()

        }
    }



    private fun loadStateFromDatabase() {
        viewModelScope.launch {
            repository.allConversionsFlow.collect { conversions ->
                if (conversions.isNotEmpty()) {
                    val values = MutableList(6) { 0.0 }
                    val currencies = MutableList(6) { "eur" }

                    conversions.forEach { conversion ->
                        if (conversion.index in 0 until 6) {
                            values[conversion.index] = conversion.amount
                            currencies[conversion.index] = conversion.selectedCurrency
                        }
                    }

                    val numberOfItems = conversions.size.coerceAtMost(6)
                    _currencyRatesState.value = ConverterUIState(
                        values = values,
                        currencies = currencies,
                        numberOfItems = numberOfItems
                    )
                }
            }
        }
    }


    fun addMoreItems() {
        _currencyRatesState.update { state ->
            if (state.numberOfItems < 6) {
                state.copy(numberOfItems = state.numberOfItems + 1)
            } else {
                state // Do nothing if 6 items are already displayed
            }
        }
    }


    private fun saveStateToDatabase() {
        viewModelScope.launch {
            val state = _currencyRatesState.value
            val conversions = mutableListOf<HomePageConversionEntity>()

            for (i in 0 until state.numberOfItems) {
                conversions.add(
                    HomePageConversionEntity(
                        index = i,
                        selectedCurrency = state.currencies[i],
                        amount = state.values[i]
                    )
                )
            }

            Log.d("ViewModel", "Inserting ${conversions.size} conversions into the database: $conversions")

            repository.deleteAllConversions()
            repository.insertConversions(conversions)

            Log.d("ViewModel", "Data inserted successfully")
        }
    }


    fun onAmountChange(newAmount: Double, index: Int) {
        viewModelScope.launch {
            Log.d("ViewModel", "onAmountChange called: newAmount = $newAmount, index = $index")

            // Step 1: Retrieve the current state
            val currentState = _currencyRatesState.value

            // Step 2: Perform computations
            val updatedValues = currentState.values.mapIndexed { i, currentValue ->
                if (i != index && currentState.currencies[i].isNotEmpty()) {
                    val converted = convertAmount(
                        amount = newAmount,
                        fromCurrency = currentState.currencies[index],
                        toCurrency = currentState.currencies[i]
                    )
                    val formattedValue = formatToTwoDecimals(converted)
                    Log.d("ConversionLog", "Converted $newAmount from ${currentState.currencies[index]} to ${currentState.currencies[i]}: $formattedValue")
                    formattedValue
                } else if (i == index) {
                    formatToTwoDecimals(newAmount)
                } else {
                    currentValue
                }
            }

            // Step 3: Update the state
            val newState = currentState.copy(values = updatedValues)
            _currencyRatesState.value = newState

            // Step 4: Save the updated state to the database
            saveStateToDatabase()
        }
    }

    fun onCurrencyChange(newCurrency: String, index: Int) {
        viewModelScope.launch {
            Log.d("ViewModel", "onCurrencyChange called: newCurrency = $newCurrency, index = $index")

            // Step 1: Retrieve the current state
            val currentState = _currencyRatesState.value

            // Step 2: Update currencies and perform computations
            val updatedCurrencies = currentState.currencies.toMutableList().apply {
                this[index] = newCurrency
            }

            val updatedValues = currentState.values.toMutableList().apply {
                if (currentState.values[index] != 0.0) {
                    val converted = convertAmount(
                        amount = currentState.values[index],
                        fromCurrency = currentState.currencies[index], // previous currency
                        toCurrency = newCurrency  // new currency
                    )
                    this[index] = formatToTwoDecimals(converted)
                }
            }

            // Step 3: Update the state
            val newState = currentState.copy(values = updatedValues, currencies = updatedCurrencies)
            _currencyRatesState.value = newState

            // Step 4: Save the updated state to the database
            saveStateToDatabase()
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

    fun removeItem(index: Int) {
        _currencyRatesState.update { state ->
            val newValues = state.values.toMutableList().apply {
                removeAt(index)
                add(0.0) // Keep the size consistent, can adjust logic as needed
            }
            val newCurrencies = state.currencies.toMutableList().apply {
                removeAt(index)
                add("eur") // Add a default currency back to keep the size consistent
            }
            val newNumberOfItems = state.numberOfItems - 1

            state.copy(
                values = newValues,
                currencies = newCurrencies,
                numberOfItems = newNumberOfItems
            )
        }
        viewModelScope.launch {
            saveStateToDatabase() // Ensure the state is persisted
        }
    }

    fun undoRemoveItem(index: Int) {

    }


}