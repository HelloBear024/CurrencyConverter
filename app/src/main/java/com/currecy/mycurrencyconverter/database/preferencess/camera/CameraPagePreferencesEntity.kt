package com.currecy.mycurrencyconverter.database.preferencess.camera

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "camera_page_preference")
data class CameraPagePreferencesEntity (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
            val firstCurrency: String = "",
            val secondCurrency: String = ""
    )
