package com.currecy.mycurrencyconverter.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.currecy.mycurrencyconverter.database.UserCurrencyPreference
import com.currecy.mycurrencyconverter.database.UserCurrencyPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CardCurrencyViewModel @Inject constructor (
    private val userRepo: UserCurrencyPreferencesRepository
) : ViewModel() {

    init {
        Log.d("CardCurrencyViewModel", "Initialized")
    }

    val conversions: StateFlow<List<ChartCurrencyState>> = userRepo.getAllPreferences()
        .map { preferences ->
            preferences.map { preference ->
                ChartCurrencyState(
                    id = preference.id,
                    sourceCurrency = preference.firstCurrencyCode,
                    targetCurrency = preference.secondCurrencyCode
                )
            }
        }
        .onEach { list ->
            Log.d("CardCurrencyViewModel", "Fetched conversions: $list")
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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
}
