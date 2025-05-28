package com.currecy.mycurrencyconverter.database.preferencess.userCurrencyList

import com.currecy.mycurrencyconverter.widgets.CurrencyPreferencesChangeListener
import kotlinx.coroutines.flow.Flow

class UserCurrencyPreferencesRepository(
    private val userCurrencyPreferenceDao: UserCurrencyPreferenceDao
) {

    private val listeners = mutableSetOf<CurrencyPreferencesChangeListener>()

    fun registerListener(listener: CurrencyPreferencesChangeListener) {
        listeners += listener
    }
    fun unregisterListener(listener: CurrencyPreferencesChangeListener) {
        listeners -= listener
    }

    private fun notifyChanged() {
        listeners.forEach { it.onPreferencesChanged() }
    }

    fun getAllPreferences(): Flow<List<UserCurrencyPreference>> = userCurrencyPreferenceDao.getAllCurrencyPreferences()

    fun getPreferenceById(id: Int): Flow<UserCurrencyPreference?> = userCurrencyPreferenceDao.getCurrencyPreferenceById(id)

    suspend fun addPreference(preference: UserCurrencyPreference) {
        userCurrencyPreferenceDao.insert(preference)
        notifyChanged()
    }

    suspend fun deletePreference(id: Int) {
        userCurrencyPreferenceDao.deleteById(id)
        notifyChanged()
    }

}