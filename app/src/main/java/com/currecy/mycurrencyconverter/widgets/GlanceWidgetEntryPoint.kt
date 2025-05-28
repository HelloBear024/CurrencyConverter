package com.currecy.mycurrencyconverter.widgets

import com.currecy.mycurrencyconverter.database.preferencess.currencyRates.CurrencyRatesRepository
import com.currecy.mycurrencyconverter.database.preferencess.userCurrencyList.UserCurrencyPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface GlanceWidgetEntryPoint {
    fun currencyRatesRepository(): CurrencyRatesRepository
    fun userCurrencyPreferences(): UserCurrencyPreferencesRepository
}