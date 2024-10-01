package com.currecy.mycurrencyconverter.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.currecy.mycurrencyconverter.database.CurrencyRateDao
import com.currecy.mycurrencyconverter.database.preferencess.camera.CameraPagePreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val currencyDao: CurrencyRateDao,
    private val preferencesRepository: CameraPagePreferencesRepository
): ViewModel() {

    private val _converterUIState = MutableStateFlow(CurrencyCameraUIState())
    val converterUIState: StateFlow<CurrencyCameraUIState> = _converterUIState.asStateFlow()


    init {
        viewModelScope.launch {
            fetchPreferencesAndUpdateState()
        }
    }


    private suspend fun fetchPreferencesAndUpdateState() {
        try {
            val prefs = preferencesRepository.getPreferences()

            // Update the UI state with the loaded preferences
            _converterUIState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    selectedCurrencyFrom = prefs.firstCurrency.ifEmpty { "usd" }, // Fallback to "usd"
                    selectedCurrencyTo = prefs.secondCurrency.ifEmpty { "eur" }   // Fallback to "eur"
                )
            }
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Error fetching preferences", e)
            _converterUIState.update { currentState ->
                currentState.copy(isLoading = false)
            }
        }
    }

    // Called when new text is detected from the camera
    suspend fun onNumberDetected(detectedText: String) {
        val number = detectedText.toDoubleOrNull()
        if (number != null) {
            _converterUIState.update { it.copy(detectedNumber = number) }
            convertCurrency()
        }
    }

     fun onCurrencyFromChange(newCurrency: String) {
        viewModelScope.launch {
            _converterUIState.update { it.copy(selectedCurrencyFrom = newCurrency) }
            preferencesRepository.updateCurrencies(newCurrency, _converterUIState.value.selectedCurrencyTo)
            convertCurrency()
        }
    }

     fun onCurrencyToChange(newCurrency: String) {
        viewModelScope.launch {
            _converterUIState.update { it.copy(selectedCurrencyTo = newCurrency) }
            preferencesRepository.updateCurrencies(_converterUIState.value.selectedCurrencyFrom, newCurrency)
            convertCurrency()
        }
    }

    fun switchCurrencies() {
        viewModelScope.launch {
            val currentFrom = _converterUIState.value.selectedCurrencyFrom
            val currentTo = _converterUIState.value.selectedCurrencyTo
            _converterUIState.update { currentState ->
                currentState.copy(
                    selectedCurrencyFrom = currentTo,
                    selectedCurrencyTo = currentFrom
                )
            }
            // Update preferences and recalculate conversion
            preferencesRepository.updateCurrencies(currentTo, currentFrom)
            convertCurrency()
        }
    }


     suspend fun convertCurrency() {
         val fromCurrency = _converterUIState.value.selectedCurrencyFrom
         val toCurrency = _converterUIState.value.selectedCurrencyTo
         val amount = _converterUIState.value.detectedNumber ?: return

         val fromRate = currencyDao.getRateForCurrency(fromCurrency)
         val toRate = currencyDao.getRateForCurrency(toCurrency)

         if (fromRate == null || toRate == null) {
             Log.d("CurrencyConversion", "Could not retrieve rates. From Rate or To Rate is null.")
         }

         if (fromRate != null && toRate != null) {
             var convertedAmount = amount * (toRate.dec() / fromRate.dec())
             convertedAmount = formatToTwoDecimals(convertedAmount)
             _converterUIState.update { it.copy(conversionResult = convertedAmount.toString()) }
         } else {
             _converterUIState.update { it.copy(conversionResult = "Conversion failed") }
         }
     }


    private fun formatToTwoDecimals(value: Double): Double {
        return "%.2f".format(value).toDouble()
    }
    }




