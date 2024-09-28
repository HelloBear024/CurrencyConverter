package com.currecy.mycurrencyconverter.database

import android.content.Context
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

}