package com.currecy.mycurrencyconverter.model.homeModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.currecy.mycurrencyconverter.database.preferencess.currencyRates.CurrencyRatesRepository
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
                    val currencies = MutableList(6) { "eur" }

                    conversions.forEach { conversion ->
                        if (conversion.index in 0 until 6) {
                            currencies[conversion.index] = conversion.selectedCurrency
                        }
                    }

                    val numberOfItems = conversions.size.coerceAtMost(6)

                    _currencyRatesState.value = ConverterUIState(
                        currencies = currencies,
                        numberOfItems = numberOfItems,
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
                        selectedCurrency = state.currencies[i]
                    )
                )
            }

            Log.d("ViewModel", "Inserting ${conversions.size} conversions into the database: $conversions")

            repository.deleteAllConversions()
            repository.insertConversions(conversions)

            Log.d("ViewModel", "Data inserted successfully")
        }
    }



    fun onAmountChange(newText: String, index: Int) {
        viewModelScope.launch {
            Log.d("ViewModel", "onAmountChange called: newText = $newText, index = $index")

            val currentState = _currencyRatesState.value

            // Create a mutable list based on the current valueTexts
            val updatedValueTexts = currentState.valueTexts.toMutableList()

            updatedValueTexts[index] = newText
            // Try to parse the new text to Double
            val newAmount = newText.toDoubleOrNull()

            if (newAmount != null) {
                // Perform conversions for other currencies
                for (i in currentState.valueTexts.indices) {
                    if (i != index && currentState.currencies[i].isNotEmpty()) {
                        val converted = convertAmount(
                            amountString = newText,
                            fromCurrency = currentState.currencies[index],
                            toCurrency = currentState.currencies[i]
                        )
                        updatedValueTexts[i] = converted
                    }
                }
            } else {
                // If parsing fails, reset other fields or handle accordingly
                for (i in currentState.valueTexts.indices) {
                    if (i != index) {
                        updatedValueTexts[i] = ""
                    }
                }
            }

            // Update the state with the updatedValueTexts which includes both new and converted values
            _currencyRatesState.update { state ->
                state.copy(valueTexts = updatedValueTexts)
            }
            Log.d("CurrencyConversion", "Attempting to convert $updatedValueTexts")

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

            val updatedValues = currentState.valueTexts.toMutableList().apply {
                if (currentState.valueTexts[index].isNotEmpty()) {
                    val converted = convertAmount(
                        amountString =  currentState.valueTexts[index],
                        fromCurrency = currentState.currencies[index],
                        toCurrency = newCurrency
                    )
                    this[index] = converted.toString()
                }
            }

            // Step 3: Update the state
            val newState = currentState.copy(valueTexts = updatedValues, currencies = updatedCurrencies)
            _currencyRatesState.value = newState

            // Step 4: Save the updated state to the database
            saveStateToDatabase()
        }
    }


    private suspend fun convertAmount(amountString: String, fromCurrency: String, toCurrency: String): String {
        Log.d("CurrencyConversion", "Attempting to convert $amountString from $fromCurrency to $toCurrency")

        val amount: Double? = amountString.toDoubleOrNull()
        if (amount == null) {
            Log.d("CurrencyConversion", "Invalid amount format: $amountString")
            return "0.00"
        }

        val fromRate: Double? = rates.getRateForCurrency(fromCurrency)
        val toRate: Double? = rates.getRateForCurrency(toCurrency)

        Log.d("CurrencyConversion", "Rates: From Rate = $fromRate, To Rate = $toRate")

        if (fromRate == null || toRate == null || fromRate == 0.0) {
            Log.d("CurrencyConversion", "Conversion failed due to null or zero rates.")
            return "0.00"
        }

        val convertedAmount = (amount / fromRate) * toRate
        val formattedAmount = formatToTwoDecimals(convertedAmount)
        Log.d("CurrencyConversion", "Converted amount: $formattedAmount")
        return formattedAmount
    }


    private fun formatToTwoDecimals(value: Double): String {
        return "%.2f".format(value)
    }

    fun removeItem(index: Int) {
        _currencyRatesState.update { state ->
            val newValues = state.valueTexts.toMutableList().apply {
                removeAt(index)
                add("") // Keep the size consistent, can adjust logic as needed
            }
            val newCurrencies = state.currencies.toMutableList().apply {
                removeAt(index)
                add("eur") // Add a default currency back to keep the size consistent
            }
            val newNumberOfItems = state.numberOfItems - 1

            state.copy(
                currencies = newCurrencies,
                numberOfItems = newNumberOfItems
            )
        }
        viewModelScope.launch {
            saveStateToDatabase()
        }
    }

    fun undoRemoveItem(index: Int) {

    }
}
