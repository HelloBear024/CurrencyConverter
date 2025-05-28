package com.currecy.mycurrencyconverter.database.preferencess.camera

import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CameraPagePreferencesRepository @Inject constructor(
    private val cameraPagePreferencesDao: CameraPagePreferencesDao
){

    suspend fun getPreferences(): CameraPagePreferencesEntity {
        return cameraPagePreferencesDao.getPreferences() ?: CameraPagePreferencesEntity()
    }

    suspend fun savePreferences(preferences: CameraPagePreferencesEntity) {
        cameraPagePreferencesDao.insertPreferences(preferences)
    }

    suspend fun updateCurrencies(firstCurrency: String, secondCurrency: String) {
        val currentPrefs = cameraPagePreferencesDao.getPreferences() ?: CameraPagePreferencesEntity()
        val updatedPrefs = currentPrefs.copy(
            firstCurrency = firstCurrency,
            secondCurrency = secondCurrency
        )
        cameraPagePreferencesDao.insertPreferences(updatedPrefs)
    }


}