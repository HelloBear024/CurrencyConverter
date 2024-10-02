package com.currecy.mycurrencyconverter.database

import android.content.Context
import com.currecy.mycurrencyconverter.database.preferencess.camera.CameraPagePreferencesDao
import com.currecy.mycurrencyconverter.database.preferencess.camera.CameraPagePreferencesRepository
import com.currecy.mycurrencyconverter.database.preferencess.currencyRates.CurrencyRateDao
import com.currecy.mycurrencyconverter.database.preferencess.currencyRates.CurrencyRatesRepository
import com.currecy.mycurrencyconverter.database.preferencess.home.HomePageConversionPreferencesRepository
import com.currecy.mycurrencyconverter.database.preferencess.home.HomePageCurrencyConversionDao
import com.currecy.mycurrencyconverter.database.preferencess.userCurrencyList.UserCurrencyPreferenceDao
import com.currecy.mycurrencyconverter.database.preferencess.userCurrencyList.UserCurrencyPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    // DAOs
    @Provides
    fun provideUserCurrencyPreferenceDao(db: AppDatabase): UserCurrencyPreferenceDao {
        return db.userCurrencyPreferenceDao()
    }

    @Provides
    fun provideCurrencyRateDao(db: AppDatabase): CurrencyRateDao {
        return db.currencyRateDao()
    }

    // Repositories
    @Provides
    @Singleton
    fun provideUserCurrencyPreferencesRepository(
        userCurrencyPreferenceDao: UserCurrencyPreferenceDao
    ): UserCurrencyPreferencesRepository {
        return UserCurrencyPreferencesRepository(userCurrencyPreferenceDao)
    }

    @Provides
    @Singleton
    fun provideCurrencyRatesRepository(
        currencyRateDao: CurrencyRateDao
    ): CurrencyRatesRepository {
        return CurrencyRatesRepository(currencyRateDao)
    }

    @Provides
    fun provideHomePageCurrencyConversionDao(appDatabase: AppDatabase): HomePageCurrencyConversionDao {
        return appDatabase.homePageCurrencyConversionDao()
    }

    @Provides
    @Singleton
    fun provideHomePageConversionPreferencesRepository(
        homePageCurrencyConversionDao: HomePageCurrencyConversionDao
    ): HomePageConversionPreferencesRepository {
        return HomePageConversionPreferencesRepository(homePageCurrencyConversionDao)
    }

    @Provides
    fun provideCameraPagePreferencesDao(database: AppDatabase): CameraPagePreferencesDao {
        return database.cameraPagePreferenceDao()
    }

    @Provides
    @Singleton
    fun provideCameraPagePreferencesRepository(
        cameraPagePreferencesDao: CameraPagePreferencesDao
    ): CameraPagePreferencesRepository {
        return CameraPagePreferencesRepository(cameraPagePreferencesDao)
    }



}