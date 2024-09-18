package com.currecy.mycurrencyconverter.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.currecy.mycurrencyconverter.R

val LeagueSpartan = FontFamily(
    Font(R.font.leaguespartan_regular)
)
val LibreBaskerville =  FontFamily(
    Font(R.font.librebaskerville_regular),
    Font(R.font.librebaskerville_bold, FontWeight.Bold)
)


// Set of Material typography styles to start with
val Typography = Typography(

    displayLarge = TextStyle(
        fontFamily = LeagueSpartan,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp
    ),
    displayMedium = TextStyle(
        fontFamily = LibreBaskerville,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    labelSmall = TextStyle(
        fontFamily = LibreBaskerville,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = LibreBaskerville,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )
)