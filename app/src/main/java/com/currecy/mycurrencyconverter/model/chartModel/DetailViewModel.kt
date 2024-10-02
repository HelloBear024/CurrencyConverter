package com.currecy.mycurrencyconverter.model.chartModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.currecy.mycurrencyconverter.api.CurrencyAPI.TimeRange
import com.currecy.mycurrencyconverter.database.preferencess.currencyRates.CurrencyRate
import com.currecy.mycurrencyconverter.database.preferencess.currencyRates.CurrencyRatesRepository
import com.currecy.mycurrencyconverter.database.preferencess.userCurrencyList.UserCurrencyPreferencesRepository
import com.currecy.mycurrencyconverter.model.searchChart.ChartCurrencyState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import javax.inject.Inject


@HiltViewModel
class DetailViewModel @Inject constructor(
    private val userRepo: UserCurrencyPreferencesRepository,
    private val ratesRepo: CurrencyRatesRepository
) : ViewModel() {


    private val _chartData = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val chartData: StateFlow<List<Pair<String, Double>>> = _chartData.asStateFlow()

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun fetchHistoricalRates(
        conversion: ChartCurrencyState,
        timeRange: TimeRange
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val endDate = LocalDate.now()
                val startDate = endDate.minusDays(timeRange.days.toLong())

                val formatter = DateTimeFormatter.ISO_DATE
                val startDateStr = startDate.format(formatter)
                val endDateStr = endDate.format(formatter)

                val ratesBetweenDates = ratesRepo.getRatesBetweenDates(startDateStr, endDateStr)

                // Group rates by date
                val ratesByDate = ratesBetweenDates.groupBy { it.date }

                // Sort dates ascending
                val sortedDates = ratesByDate.keys.sorted()

                // Prepare list of dates with calculated base -> target rates
                val calculatedRates = sortedDates.mapNotNull { date ->
                    val ratesForDate = ratesByDate[date] ?: emptyList()
                    val calculatedRate = calculateExchangeRate(
                        baseCurrency = conversion.sourceCurrency,
                        targetCurrency = conversion.targetCurrency,
                        ratesForDate = ratesForDate
                    )
                    calculatedRate?.let { Pair(date, it) }
                }

                _chartData.value = calculatedRates
                _uiState.value = _uiState.value.copy(chartData = calculatedRates, isLoading = false)
            } catch (e: Exception) {
                Log.e("DetailViewModel", "Error fetching historical rates", e)
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Failed to load data.")
            }
        }
    }
}

private fun calculateExchangeRate(
    baseCurrency: String,
    targetCurrency: String,
    ratesForDate: List<CurrencyRate>
): Double? {
    val baseRate = ratesForDate.find { it.currencyCode.equals(baseCurrency, ignoreCase = true) }?.rate
    val targetRate = ratesForDate.find { it.currencyCode.equals(targetCurrency, ignoreCase = true) }?.rate

    return if (baseRate != null && targetRate != null && baseRate != 0.0) {
        targetRate / baseRate
    } else {
        null
    }
}

data class HistoricalRate(
    val date: Date,
    val rate: Double
)

data class DetailUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val chartData: List<Pair<String, Double>> = emptyList()
)

