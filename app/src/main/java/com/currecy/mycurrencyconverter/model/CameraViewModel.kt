package com.currecy.mycurrencyconverter.model

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

    suspend fun onCurrencyChange(newCurrency: String, index: Int) {
        if (index == 0) {
            _converterUIState.update { it.copy(selectedCurrencyFrom = newCurrency) }
        } else {
            _converterUIState.update { it.copy(selectedCurrencyTo = newCurrency) }
        }
        convertCurrency()
    }

     suspend fun convertCurrency(): String {
        val rates = currencyDao.getRateForCurrency(
            _converterUIState.value.selectedCurrencyFrom,
        )

        return ""
    }

    fun getConversionRate(): String {
        return ""
    }


}