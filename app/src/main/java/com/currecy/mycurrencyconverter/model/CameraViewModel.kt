package com.currecy.mycurrencyconverter.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.currecy.mycurrencyconverter.database.CurrencyRateDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class CameraViewModel(
    private val currencyDao: CurrencyRateDao
): ViewModel() {

    private val _converterUIState = MutableStateFlow(CurrencyCameraUIState())
    val converterUIState: StateFlow<CurrencyCameraUIState> = _converterUIState

    // Called when new text is detected from the camera
    suspend fun onNumberDetected(detectedText: String) {
        val number = detectedText.toDoubleOrNull()
        if (number != null) {
            _converterUIState.update { it.copy(detectedNumber = number) }
            convertCurrency()
        }
    }

    suspend fun onCurrencyFromChange(newCurrency: String) {
        _converterUIState.update { it.copy(selectedCurrencyFrom = newCurrency) }
        convertCurrency()
    }

    suspend fun onCurrencyToChange(newCurrency: String) {
        _converterUIState.update { it.copy(selectedCurrencyTo = newCurrency) }
        convertCurrency()
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




