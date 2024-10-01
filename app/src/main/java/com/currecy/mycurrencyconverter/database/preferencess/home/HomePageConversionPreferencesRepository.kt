package com.currecy.mycurrencyconverter.database.preferencess.home

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class HomePageConversionPreferencesRepository @Inject constructor(
    private val homePageCurrencyConversionDao: HomePageCurrencyConversionDao
){


    val allConversionsFlow: Flow<List<HomePageConversionEntity>> =
        homePageCurrencyConversionDao.getAllConversionsFlow()

    suspend fun insertConversion(conversion: HomePageConversionEntity) {
        homePageCurrencyConversionDao.insertConversion(conversion)
    }

    suspend fun insertConversions(conversions: List<HomePageConversionEntity>) {
        homePageCurrencyConversionDao.insertConversions(conversions)
    }

    suspend fun updateConversion(conversion: HomePageConversionEntity) {
        homePageCurrencyConversionDao.updateConversion(conversion)
    }

    suspend fun deleteConversion(conversion: HomePageConversionEntity) {
        homePageCurrencyConversionDao.deleteConversion(conversion)
    }

    suspend fun deleteAllConversions() {
        homePageCurrencyConversionDao.deleteAllConversions()
    }
}