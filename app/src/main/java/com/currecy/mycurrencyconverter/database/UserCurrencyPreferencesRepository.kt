package com.currecy.mycurrencyconverter.database

import kotlinx.coroutines.flow.Flow

class UserCurrencyPreferencesRepository(
    private val userCurrencyPreferenceDao: UserCurrencyPreferenceDao
) {

    fun getAllPreferences(): Flow<List<UserCurrencyPreference>> = userCurrencyPreferenceDao.getAllCurrencyPreferences()

    fun getPreferenceById(id: Int): Flow<UserCurrencyPreference?> = userCurrencyPreferenceDao.getCurrencyPreferenceById(id)

    suspend fun addPreference(preference: UserCurrencyPreference) = userCurrencyPreferenceDao.insert(preference)

    suspend fun deletePreference(id: Int) = userCurrencyPreferenceDao.deleteById(id)

}