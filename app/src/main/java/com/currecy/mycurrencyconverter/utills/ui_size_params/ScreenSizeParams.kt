package com.currecy.mycurrencyconverter.utills.ui_size_params

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

enum class ScreenSize {
    SMALL, NORMAL, TABLET, XLARGE
}

@Composable
fun getScreenSize(): ScreenSize {
    val configuration = LocalConfiguration.current
    return when {
        configuration.screenWidthDp < 360 -> ScreenSize.SMALL
        configuration.screenWidthDp in 360..600 -> ScreenSize.NORMAL
        configuration.screenWidthDp in 601..840 -> ScreenSize.TABLET
        else -> ScreenSize.XLARGE
    }
}