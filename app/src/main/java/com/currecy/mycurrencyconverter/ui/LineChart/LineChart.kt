package com.currecy.mycurrencyconverter.ui.LineChart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.currecy.mycurrencyconverter.model.HistoricalRate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun ExchangeRateLineChart(
    chartData: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    lineWidth: Float = 4f,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    axisColor: Color = Color.Black,
    labelColor: Color = MaterialTheme.colorScheme.tertiary,
    markerColor: Color = MaterialTheme.colorScheme.secondary,
    tooltipColor: Color = Color.Black.copy(alpha = 0.7f),
    tooltipTextColor: Color = Color.White,
    numberOfYTicks: Int = 5
) {

    if (chartData.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available for the selected time range.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    // Convert chartData to HistoricalRate list
    val historicalRates = chartData.map { it.toHistoricalRate() }

    // Define padding for the chart area
    val padding = 40.dp

    // State for selected point (for tooltip)
    var selectedPoint by remember { mutableStateOf<HistoricalRate?>(null) }

    // Convert padding from dp to pixels
    val density = LocalDensity.current
    val paddingPx = with(density) { padding.toPx() }



    Box(
        modifier = modifier
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    // Find the closest point to the tap
                    val width = size.width - 2 * paddingPx
                    val height = size.height - 2 * paddingPx
                    val minY = historicalRates.minOfOrNull { it.rate } ?: 0.0
                    val maxY = historicalRates.maxOfOrNull { it.rate } ?: 1.0
                    val xScale = width / (historicalRates.size - 1)
                    val yScale = height / (maxY - minY)

                    val gradientBrush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.4f), // Start with a semi-transparent version of the line color
                            backgroundColor // End with the background color
                        ),
                        startY = paddingPx, // Start the gradient at the top of the chart
                        endY = size.height - paddingPx // End the gradient at the bottom of the chart
                    )

                    // Calculate the index of the nearest data point based on tap X
                    val tappedIndex = ((tapOffset.x - paddingPx) / xScale).roundToInt().coerceIn(0, historicalRates.size - 1)
                    val nearestPoint = historicalRates[tappedIndex]

                    // Calculate the position of the nearest point
                    val pointX = paddingPx + tappedIndex * xScale
                    val pointY = size.height - paddingPx - ((nearestPoint.rate - minY) * yScale).toFloat()

                    // Calculate distance from tap to the point
                    val distance = sqrt(
                        (tapOffset.x - pointX).pow(2) +
                                (tapOffset.y - pointY).pow(2)
                    )

                    val threshold = 50f // Adjust threshold as needed
                    if (distance <= threshold) {
                        selectedPoint = nearestPoint
                    } else {
                        selectedPoint = null
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Determine the drawable area
            val width = size.width - 2 * paddingPx
            val height = size.height - 2 * paddingPx

            // Find min and max rates
            val minRate = historicalRates.minOfOrNull { it.rate } ?: 0.0
            val maxRate = historicalRates.maxOfOrNull { it.rate } ?: 1.0

            // Define scales
            val xScale = width / (historicalRates.size - 1)
            val yScale = height / (maxRate - minRate)

            // Draw background
            drawRect(color = backgroundColor)

            // Draw grid lines (optional)
            for (i in 0..numberOfYTicks) {
                val yValue = minRate + (maxRate - minRate) / numberOfYTicks * i
                val yPos = size.height - paddingPx - ((yValue - minRate) * yScale).toFloat()

                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(paddingPx, yPos),
                    end = Offset(size.width - paddingPx, yPos),
                    strokeWidth = 1f
                )
            }

            val gradientBrush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.4f),  // Semi-transparent at the top
                    backgroundColor                // Full background color at the bottom
                ),
                startY = paddingPx,                  // Start gradient at the top of the drawable area
                endY = size.height - paddingPx       // End gradient at the bottom of the drawable area
            )

            // Draw axes
            drawLine(
                color = axisColor,
                start = Offset(paddingPx, paddingPx),
                end = Offset(paddingPx, size.height - paddingPx),
                strokeWidth = 2f
            )
            drawLine(
                color = axisColor,
                start = Offset(paddingPx, size.height - paddingPx),
                end = Offset(size.width - paddingPx, size.height - paddingPx),
                strokeWidth = 2f
            )

            // Draw Y-axis labels and ticks
            for (i in 0..numberOfYTicks) {
                val yValue = minRate + (maxRate - minRate) / numberOfYTicks * i
                val yPos = size.height - paddingPx - ((yValue - minRate) * yScale).toFloat()

                // Draw tick
                drawLine(
                    color = axisColor,
                    start = Offset(paddingPx - 10f, yPos),
                    end = Offset(paddingPx, yPos),
                    strokeWidth = 2f
                )

                // Draw label
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        String.format("%.2f", yValue),
                        paddingPx - 15f,
                        yPos + 5f,
                        android.graphics.Paint().apply {
                            color = labelColor.toArgb()
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.RIGHT
                            isAntiAlias = true
                        }
                    )
                }
            }

            // Draw X-axis labels and ticks
            val labelStep = (historicalRates.size / 5).coerceAtLeast(1)
            for (i in historicalRates.indices step labelStep) {
                val xValue = i
                val xPos = paddingPx + i * xScale

                // Draw tick
                drawLine(
                    color = axisColor,
                    start = Offset(xPos, size.height - paddingPx),
                    end = Offset(xPos, size.height - paddingPx + 10f),
                    strokeWidth = 2f
                )

                // Draw label
                val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                val label = outputFormat.format(historicalRates[i].date)
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        label,
                        xPos,
                        size.height - paddingPx + 30f,
                        android.graphics.Paint().apply {
                            color = labelColor.toArgb()
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                    )
                }
            }


            // Plot the line and shaded area


            val path = Path()
            val shadedPath = Path()
            historicalRates.forEachIndexed { index, point ->
                val x = paddingPx + index * xScale
                val y = size.height - paddingPx - ((point.rate - minRate) * yScale).toFloat()
                if (index == 0) {
                    path.moveTo(x, y)
                    shadedPath.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                    shadedPath.lineTo(x, y)
                }
            }
            // Close the shaded path
            shadedPath.lineTo(paddingPx + (historicalRates.size - 1) * xScale, size.height - paddingPx)
            shadedPath.lineTo(paddingPx, size.height - paddingPx)
            shadedPath.close()

            // Draw shaded area
            drawPath(
                path = shadedPath,
                brush = gradientBrush,
                style = Fill
            )

            // Draw the line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = lineWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Draw markers and labels
            historicalRates.forEach { point ->
                val index = historicalRates.indexOf(point)
                val x = paddingPx + index * xScale
                val y = size.height - paddingPx - ((point.rate - minRate) * yScale).toFloat()

                // Draw marker
                drawCircle(
                    color = markerColor,
                    radius = 8f,
                    center = Offset(x, y)
                )


            }

            // Draw tooltip if a point is selected
            selectedPoint?.let { point ->
                val index = historicalRates.indexOf(point)
                val x = paddingPx + index * xScale
                val y = size.height - paddingPx - ((point.rate - minRate) * yScale).toFloat()

                // Tooltip dimensions
                val tooltipWidth = 150f
                val tooltipHeight = 60f

                // Tooltip position
                var tooltipX = x - tooltipWidth / 2
                var tooltipY = y - tooltipHeight - 20f

                // Ensure tooltip stays within bounds
                tooltipX = tooltipX.coerceIn(10f, size.width - tooltipWidth - 10f)
                tooltipY = tooltipY.coerceAtLeast(10f)

                // Draw tooltip background
                drawRoundRect(
                    color = tooltipColor,
                    topLeft = Offset(tooltipX, tooltipY),
                    size = Size(tooltipWidth, tooltipHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Draw tooltip text
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "${String.format("%.4f", point.rate)}",
//                        "${point.date}: ${String.format("%.4f", point.rate)}",
                        tooltipX + tooltipWidth / 2,
                        tooltipY + tooltipHeight / 2 + 10f,
                        android.graphics.Paint().apply {
                            color = tooltipTextColor.toArgb()
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                    )
                }

                drawLine(
                    color = Color.Black,
                    start = Offset(x, y),
                    end = Offset(x, tooltipY + tooltipHeight),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }
        }
    }
}



fun Pair<String, Double>.toHistoricalRate(): HistoricalRate {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = inputFormat.parse(this.first) ?: Date()
    return HistoricalRate(date, this.second)
}
