package com.currecy.mycurrencyconverter.database.preferencess.home

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HomePageCurrencyConversionDao {

    @Query("SELECT * FROM home_currency_conversion ORDER BY `index` ASC")
    fun getAllConversionsFlow(): Flow<List<HomePageConversionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversion(conversion: HomePageConversionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversions(conversions: List<HomePageConversionEntity>)

    @Update
    suspend fun updateConversion(conversion: HomePageConversionEntity)

    @Delete
    suspend fun deleteConversion(conversion: HomePageConversionEntity)

    @Query("DELETE FROM home_currency_conversion")
    suspend fun deleteAllConversions()
}