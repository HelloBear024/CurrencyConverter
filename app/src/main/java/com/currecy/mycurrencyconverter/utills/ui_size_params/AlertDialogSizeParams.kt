package com.currecy.mycurrencyconverter.utills.ui_size_params

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun alertDialogHeaderFontSize() = when (getScreenSize()) {
    ScreenSize.TABLET -> 50.sp
    ScreenSize.SMALL -> 128.sp
    else -> 28.sp
}

@Composable
fun alertDialogBodyFontSize() = when (getScreenSize()) {
    ScreenSize.TABLET -> 30.sp
    ScreenSize.SMALL -> 20.sp
    else -> 20.sp
}

@Composable
fun alertDialogTextStartPadding() = when (getScreenSize()) {
    ScreenSize.TABLET -> 15.dp
    ScreenSize.SMALL -> 5.dp
    else -> 0.dp
}
@Composable
fun alertDialogTextTopPadding() = when (getScreenSize()) {
    ScreenSize.TABLET -> 8.dp
    ScreenSize.SMALL -> 0.dp
    else -> 8.dp
}

@Composable
fun alertDialogTextBottomPadding() = when (getScreenSize()) {
    ScreenSize.TABLET -> 8.dp
    ScreenSize.SMALL -> 0.dp
    else -> 0.dp
}