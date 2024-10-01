package com.currecy.mycurrencyconverter.database.preferencess.camera

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CameraPagePreferencesDao {

    @Query("SELECT * FROM camera_page_preference LIMIT 1")
    suspend fun getPreferences(): CameraPagePreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(preferences: CameraPagePreferencesEntity)

    @Update
    suspend fun updatePreferences(preferences: CameraPagePreferencesEntity)

}