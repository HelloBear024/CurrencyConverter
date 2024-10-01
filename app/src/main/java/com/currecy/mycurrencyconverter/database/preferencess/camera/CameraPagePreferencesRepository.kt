package com.currecy.mycurrencyconverter.database.preferencess.camera

import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CameraPagePreferencesRepository @Inject constructor(
    private val cameraPagePreferencesDao: CameraPagePreferencesDao
){

    /**
     * Retrieves the current preferences. If none exist, returns a default entity.
     */
    suspend fun getPreferences(): CameraPagePreferencesEntity {
        return cameraPagePreferencesDao.getPreferences() ?: CameraPagePreferencesEntity()
    }

    /**
     * Saves the provided preferences to the database.
     */
    suspend fun savePreferences(preferences: CameraPagePreferencesEntity) {
        cameraPagePreferencesDao.insertPreferences(preferences)
    }

    /**
     * Updates the currencies in the preferences.
     */
    suspend fun updateCurrencies(firstCurrency: String, secondCurrency: String) {
        val currentPrefs = cameraPagePreferencesDao.getPreferences() ?: CameraPagePreferencesEntity()
        val updatedPrefs = currentPrefs.copy(
            firstCurrency = firstCurrency,
            secondCurrency = secondCurrency
        )
        cameraPagePreferencesDao.insertPreferences(updatedPrefs) // Using REPLACE strategy
    }



}