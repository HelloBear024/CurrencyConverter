package com.currecy.mycurrencyconverter.model.searchChart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.currecy.mycurrencyconverter.database.preferencess.currencyRates.CurrencyRatesRepository
import com.currecy.mycurrencyconverter.database.preferencess.userCurrencyList.UserCurrencyPreference
import com.currecy.mycurrencyconverter.database.preferencess.userCurrencyList.UserCurrencyPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardCurrencyViewModel @Inject constructor (
    private val userRepo: UserCurrencyPreferencesRepository,
    private val ratesRepo: CurrencyRatesRepository
) : ViewModel() {


    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        Log.d("CardCurrencyViewModel", "Initialized")
    }


    val conversions: StateFlow<List<ChartCurrencyState>> = userRepo.getAllPreferences()
        .map { preferences ->
            preferences.map { preference ->

                // Fetch the two most recent rates for both currencies
                val firstRates = ratesRepo.getTwoMostRecentRates(preference.firstCurrencyCode)
                val secondRates = ratesRepo.getTwoMostRecentRates(preference.secondCurrencyCode)

                val firstCurrentRate = firstRates.getOrNull(0)?.rate ?: 0.0
                val firstPreviousRate = firstRates.getOrNull(1)?.rate ?: 0.0

                val secondCurrentRate = secondRates.getOrNull(0)?.rate ?: 0.0
                val secondPreviousRate = secondRates.getOrNull(1)?.rate ?: 0.0

                Log.d("CardCurrencyViewModel", "First Current Rate ($firstCurrentRate), First Previous Rate ($firstPreviousRate)")
                Log.d("CardCurrencyViewModel", "Second Current Rate ($secondCurrentRate), Second Previous Rate ($secondPreviousRate)")

                // Calculate the combined exchange rate for the two most recent rates
                val todayRate = if (firstCurrentRate != 0.0) {
                    secondCurrentRate / firstCurrentRate
                } else 0.0

                Log.d("CardCurrencyViewModel", "todayRate : ($todayRate)")

                val yesterdayRate = if (firstPreviousRate != 0.0) {
                    secondPreviousRate / firstPreviousRate
                } else 0.0

                Log.d("CardCurrencyViewModel", "yesterdayRate : ($yesterdayRate)")

                // Calculate the percentage change in the combined exchange rate
                val percentageChange = if (yesterdayRate > 0) {
                    ((todayRate - yesterdayRate) / yesterdayRate) * 100
                } else {
                    0.0
                }

                Log.d("CardCurrencyViewModel", "Percentage Change: $percentageChange")

                ChartCurrencyState(
                    id = preference.id,
                    sourceCurrency = preference.firstCurrencyCode,
                    targetCurrency = preference.secondCurrencyCode,
                    currentRate = todayRate,
                    percentageChange = percentageChange
                )
            }
        }
        .onEach { list ->
            Log.d("CardCurrencyViewModel", "Fetched conversions: $list")
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


//    val conversions: StateFlow<List<ChartCurrencyState>> = userRepo.getAllPreferences()
//        .map { preferences ->
//            preferences.map { preference ->
//
//                val firstCurrentRate = ratesRepo.getCurrentRate(preference.firstCurrencyCode) ?: 0.0
//                val firstPreviousRate = ratesRepo.getPreviousRate(preference.firstCurrencyCode) ?: 0.0
//
//                val secondCurrentRate = ratesRepo.getCurrentRate(preference.secondCurrencyCode) ?: 0.0
//                val secondPreviousRate = ratesRepo.getPreviousRate(preference.secondCurrencyCode) ?: 0.0
//
//                Log.d("CardCurrencyViewModel", "First Current Rate ($firstCurrentRate), First Previous Rate ($firstPreviousRate)")
//                Log.d("CardCurrencyViewModel", "Second Current Rate ($secondCurrentRate), Second Previous Rate ($secondPreviousRate)")
//
//
//                // Calculate the combined exchange rate for today (USD/EUR)
//                val todayRate = if (secondCurrentRate != 0.0) {
//                    secondCurrentRate / firstCurrentRate
//                } else 0.0
//
//                Log.d("CardCurrencyViewModel", "todayRate : ($todayRate)")
//
//
//                // Calculate the combined exchange rate for yesterday (USD/EUR)
//                val yesterdayRate = if (secondPreviousRate != 0.0) {
//                    secondPreviousRate / firstPreviousRate
//                } else 0.0
//
//                Log.d("CardCurrencyViewModel", "yesterdayRate : ($yesterdayRate)")
//
//
//                // Calculate the percentage change in the combined exchange rate
//                val percentageChange = if (yesterdayRate > 0) {
//                    ((todayRate - yesterdayRate) / yesterdayRate) * 100
//                } else {
//                    0.0
//                }
//
//                Log.d("CardCurrencyViewModel", "Percentage Change: $percentageChange")
//
//                ChartCurrencyState(
//                    id = preference.id,
//                    sourceCurrency = preference.firstCurrencyCode,
//                    targetCurrency = preference.secondCurrencyCode,
//                    currentRate = todayRate,
//                    percentageChange = percentageChange
//
//                )
//            }
//        }
//        .onEach { list ->
//            Log.d("CardCurrencyViewModel", "Fetched conversions: $list")
//        }
//        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Function to add a new conversion
    fun addConversion(source: String, target: String) {
        Log.d("CardCurrencyViewModel", "Adding conversion: $source -> $target")
        viewModelScope.launch {try {
            userRepo.addPreference(
                UserCurrencyPreference(
                    firstCurrencyCode = source,
                    secondCurrencyCode = target
                )
            )
            Log.d("CardCurrencyViewModel", "Conversion added successfully")
        } catch (e: Exception) {
            Log.e("CardCurrencyViewModel", "Error adding conversion", e)
        }
        }
    }

    fun getConversionById(id: Int): Flow<ChartCurrencyState?> {
        return userRepo.getPreferenceById(id)
            .map { preference ->
                preference?.let {
                    ChartCurrencyState(
                        id = it.id,
                        sourceCurrency = it.firstCurrencyCode,
                        targetCurrency = it.secondCurrencyCode
                    )
                }
            }
    }



    fun deleteConversion(conversion: ChartCurrencyState) {
        Log.d("CardCurrencyViewModel", "Deleting conversion: ${conversion.id}")
        viewModelScope.launch {
            try {
                userRepo.deletePreference(conversion.id)
                Log.d("CardCurrencyViewModel", "Conversion deleted successfully")
            } catch (e: Exception) {
                Log.e("CardCurrencyViewModel", "Error deleting conversion", e)
            }
        }
    }


    // **Filtered Conversions Based on Search Query**
    val filteredConversions: StateFlow<List<ChartCurrencyState>> = combine(conversions, _searchQuery) { conversions, query ->
        if (query.isBlank()) {
            conversions
        } else {
            conversions.filter {
                it.sourceCurrency.contains(query, ignoreCase = true) ||
                        it.targetCurrency.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // **Function to Update the Search Query**
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }




}

