package com.currecy.mycurrencyconverter.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.glance.BitmapImageProvider
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.currecy.mycurrencyconverter.MainActivity
import com.currecy.mycurrencyconverter.model.searchChart.ChartCurrencyState
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.math.RoundingMode


class ConversionWidget: GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        Log.d("ConversionWidget", "▶ provideGlance() for widget $id")

        Log.d("WidgetUpdateRegistrar", "Widget.updateAll() complete")

        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            GlanceWidgetEntryPoint::class.java
        )


        val currencyRatesRepo = entryPoint.currencyRatesRepository()
        val userSelectedCurrency = entryPoint.userCurrencyPreferences()

        val preferences = userSelectedCurrency.getAllPreferences().first()



        val conversions = preferences.map { pref ->
            val ratesA = currencyRatesRepo.getTwoMostRecentRates(pref.firstCurrencyCode)
            val ratesB = currencyRatesRepo.getTwoMostRecentRates(pref.secondCurrencyCode)

            val todayRate     = (ratesB.getOrNull(0)?.rate ?: 0.0) /
                    (ratesA.getOrNull(0)?.rate ?: Double.MAX_VALUE)

            val yesterdayRate = (ratesB.getOrNull(1)?.rate ?: 0.0) /
                    (ratesA.getOrNull(1)?.rate ?: Double.MAX_VALUE)

            val pctChange = if (yesterdayRate > 0)
                (todayRate - yesterdayRate) / yesterdayRate * 100
            else 0.0

            ChartCurrencyState(
                id               = pref.id,
                sourceCurrency   = pref.firstCurrencyCode,
                targetCurrency   = pref.secondCurrencyCode,
                currentRate      = todayRate,
                percentageChange = pctChange
            )
        }

        provideContent {
            CurrencyConverterContent(
                conversionsList = conversions
            )
        }
    }



    @Composable
    fun CurrencyConverterContent(
        conversionsList: List<ChartCurrencyState>
    ) {

        Column(
            modifier = GlanceModifier
                .cornerRadius(20.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .background( Color(0x4DFFFFFF))
                .clickable(actionStartActivity<MainActivity>())
        ) {
            LazyColumn {
                items (conversionsList){ individualConversion ->
                    Box(
                        modifier = GlanceModifier
                            .padding(vertical = 6.dp)
                    ) {
                    Box (
                        modifier = GlanceModifier
                            .padding(vertical = 8.dp, horizontal = 8.dp)
                            .background( Color(0x99FFFFFF))
                            .cornerRadius(25.dp)
                            .fillMaxWidth()

                    ) {
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${individualConversion.sourceCurrency.uppercase()}/${individualConversion.targetCurrency.uppercase()}",
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorProvider(
                                        day = Color(0xFFFFFFFF),
                                        night = Color(0xFFFFFFFF)
                                    )
                                ),
                                modifier = GlanceModifier.defaultWeight()
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${
                                        BigDecimal(individualConversion.currentRate).setScale(
                                            4,
                                            RoundingMode.HALF_EVEN
                                        )
                                    }",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ColorProvider(
                                            day = Color(0xFFFFFFFF),
                                            night = Color(0xFFFFFFFF)
                                        )
                                    )
                                )

                                Box(
                                    modifier = GlanceModifier.width(70.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {

                                    Box(
                                        modifier = GlanceModifier
                                            .background(
                                                color = if (individualConversion.percentageChange >= 0) Color(
                                                    0x80077D07
                                                ) else Color(0x99FD5B66)
                                            )
                                            .cornerRadius(20.dp)
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                    ) {
                                        Text(
                                            text = "${if (individualConversion.percentageChange >= 0) "+" else ""}${
                                                String.format(
                                                    "%.2f",
                                                    individualConversion.percentageChange
                                                )
                                            }%",
                                            style = TextStyle(
                                                fontSize = 12.sp,
                                                color = ColorProvider(
                                                    day = Color(0xFFFFFFFF),
                                                    night = Color(0xFFFFFFFF)
                                                )
                                            )
                                        )
                                    }
                                }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("RestrictedApi")
fun GlanceModifier.cornerRadiusCompat(
    cornerRadius: Int,
    @ColorInt color: Int,
    @FloatRange(from = 0.0, to = 1.0) backgroundAlpha: Float = 1f,
): GlanceModifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.background(Color(color).copy(alpha = backgroundAlpha))
            .cornerRadius(cornerRadius.dp)
    } else {
        val radii = FloatArray(8) { cornerRadius.toFloat() }
        val shape = ShapeDrawable(RoundRectShape(radii, null, null))
        shape.paint.color = ColorUtils.setAlphaComponent(color, (255 * backgroundAlpha).toInt())
        val bitmap = shape.toBitmap(width = 150, height = 75)
        this.background(BitmapImageProvider(bitmap))
    }
}
