package com.currecy.mycurrencyconverter.ui


import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.currecy.mycurrencyconverter.R
import com.currecy.mycurrencyconverter.api.CurrencyAPI.TimeRange
import com.currecy.mycurrencyconverter.model.chartModel.DetailUiState
import com.currecy.mycurrencyconverter.model.chartModel.DetailViewModel
import com.currecy.mycurrencyconverter.model.chartModel.NewsViewModel
import com.currecy.mycurrencyconverter.model.searchChart.CardCurrencyViewModel
import com.currecy.mycurrencyconverter.ui.LineChart.ExchangeRateLineChart
import com.currecy.mycurrencyconverter.ui.News.NewsItem
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Composable
fun DetailScreen(
    conversionId: String,
    navController: NavController,
    hazeState: HazeState,
    cardCurrencyViewModel: CardCurrencyViewModel = hiltViewModel(),
    detailViewModel: DetailViewModel = hiltViewModel(),
    newsViewModel: NewsViewModel = hiltViewModel()
) {

    val scrollState = rememberLazyListState()

    val conversion by cardCurrencyViewModel.getConversionById(conversionId.toInt())
        .collectAsState(initial = null)
    val uiState by detailViewModel.uiState.collectAsState()
    val newsArticles by newsViewModel.newsArticles.collectAsState()



    LaunchedEffect(conversion?.sourceCurrency, conversion?.targetCurrency) {
        conversion?.let {
            newsViewModel.fetchNews(it.sourceCurrency, it.targetCurrency)
        }
    }

    val chartVisibilityState = remember {
        MutableTransitionState(true).apply {
            targetState =
                scrollState.firstVisibleItemIndex == 0 && scrollState.firstVisibleItemScrollOffset < 200
        }
    }

    LaunchedEffect(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset) {
        chartVisibilityState.targetState =
            scrollState.firstVisibleItemIndex == 0 && scrollState.firstVisibleItemScrollOffset < 400
    }


    // Current selected time range, default to 30 days
    var selectedTimeRange by remember { mutableStateOf(TimeRange.THIRTY_DAYS) }

    // Fetch historical rates when the screen is displayed or time range changes
    LaunchedEffect(conversion, selectedTimeRange) {
        conversion?.let {
            detailViewModel.fetchHistoricalRates(it, selectedTimeRange)
        }
    }


    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)

    val statusBarDp = with(density) { statusBarHeight.toDp() }


    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { _ ->

        AsyncImage(
            model = R.drawable.background_new,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
        )

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
                .zIndex(3f)
        ) {
            val maxWith = this.maxWidth
            val maxHeight = this.maxHeight

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp + statusBarDp),
                contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${conversion?.sourceCurrency?.uppercase() ?: ""} / ${conversion?.targetCurrency?.uppercase() ?: "" }",
                        fontSize = 30.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color(0xFFFFFFFF),
                        modifier = Modifier
                    )
                }


            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                    .align(Alignment.BottomCenter)
                    .height(maxHeight / 1.11f)
                    .width(maxWith)
                    .background(Color(0x99FFFFFF)),
            ) {

                if (conversion != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding()
                            .padding(top = 5.dp, start = 12.dp, end = 12.dp)
                    ) {
                        // Display Conversion Details
                        Spacer(modifier = Modifier.height(8.dp))

                        ChartSection(
                            uiState = uiState,
                            chartVisibilityState = chartVisibilityState,
                            selectedTimeRange = selectedTimeRange,
                            onTimeRangeSelected = { timeRange ->
                                selectedTimeRange = timeRange
                            },
                            hazeState = hazeState
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .hazeEffect(
                                    state = hazeState,
                                    style = HazeMaterials.ultraThin()
                                ) {
                                    blurRadius = 30.dp
                                    noiseFactor
                                }
                        ) {
                        if (newsArticles.isNotEmpty()) {
                            LazyColumn(
                                state = scrollState,
                                modifier = Modifier.fillMaxSize().padding(8.dp)
                            ) {
                                if (newsArticles.isNotEmpty()) {

                                    item {
                                        Text(
                                            text = "Related News",
                                            style = MaterialTheme.typography.headlineMedium,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }


                                    items(newsArticles) { article ->

                                            NewsItem(article)
                                        }


                                } else {
                                    item {
                                        Text(
                                            text = "No news available for ${conversion?.sourceCurrency?.uppercase() ?: ""}.",
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Conversion not found",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun ChartSection(
    uiState: DetailUiState,
    chartVisibilityState: MutableTransitionState<Boolean>,
    selectedTimeRange: TimeRange,
    onTimeRangeSelected: (TimeRange) -> Unit,
    hazeState: HazeState
) {
    AnimatedVisibility(visibleState = chartVisibilityState) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .fillMaxWidth()
                .background(Color(0x99FFFFFF))
                .hazeEffect(
                    state = hazeState,
                    style = HazeMaterials.ultraThin()
                ) {
                    blurRadius = 30.dp
                    noiseFactor
                }
        ) {
            Column {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.errorMessage != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.errorMessage!!,
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    else -> {
                        if (uiState.chartData.isNotEmpty()) {
                            ExchangeRateLineChart(
                                chartData = uiState.chartData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                lineColor = MaterialTheme.colorScheme.primary,
                                markerColor = MaterialTheme.colorScheme.secondary
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No data available for the selected time range.",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        TimeRangeSelection(
                            selectedTimeRange = selectedTimeRange,
                            onTimeRangeSelected = onTimeRangeSelected
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}




@Composable
fun TimeRangeSelection(
    selectedTimeRange: TimeRange,
    onTimeRangeSelected: (TimeRange) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TimeRange.values().forEach { timeRange ->
            Button(
                onClick = { onTimeRangeSelected(timeRange) },
                colors = ButtonDefaults.buttonColors(

                    containerColor = if (timeRange == selectedTimeRange) Color(0x66FD5B66) else Color.White,
                )
            ) {
                Text(
                    text = when (timeRange) {
                        TimeRange.THREE_DAYS -> "3D"
                        TimeRange.SEVEN_DAYS -> "7D"
                        TimeRange.TWO_WEEKS -> "2W"
                        TimeRange.THIRTY_DAYS -> "30D"
                    },
                    color = if (timeRange == selectedTimeRange) Color.White else Color(0x66FD5B66)
                )
            }
        }
    }
}

@Composable
fun ChartSectionNew(
    uiState: DetailUiState,
    chartVisibilityState: MutableTransitionState<Boolean>,
    selectedTimeRange: TimeRange,
    onTimeRangeSelected: (TimeRange) -> Unit,
    hazeState: HazeState
) {
    AnimatedVisibility(visibleState = chartVisibilityState) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .fillMaxWidth()
                .hazeEffect(
                    state = hazeState,
                    style = HazeMaterials.ultraThin()
                ) {
                    blurRadius = 30.dp
                    noiseFactor
                }
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                when {
                    uiState.isLoading -> {  }
                    uiState.errorMessage != null -> {}
                    else -> {

                        if (uiState.chartData.isNotEmpty()) {

                            val lines = remember(uiState.chartData) {
                                listOf(
                                    Line(
                                        label = "${selectedTimeRange.name} rate",
                                        values = uiState.chartData.map { it.second },
                                        color = SolidColor(Color(0xFF23af92)),
                                        firstGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .5f),
                                        secondGradientFillColor = Color.Transparent,
                                        drawStyle = DrawStyle.Stroke(width = 2.dp),
                                        dotProperties = DotProperties(enabled = true),
                                        curvedEdges = true,
                                    )
                                )
                            }

                            val inputFormatter  = DateTimeFormatter.ISO_LOCAL_DATE
                            val outputFormatter = DateTimeFormatter.ofPattern("d")


                            val dateLabels = remember(uiState.chartData) {
                                uiState.chartData.map { (dateString, _) ->
                                    LocalDate
                                        .parse(dateString, inputFormatter)
                                        .format(outputFormatter)
                                }
                            }




                            val labelProps = LabelProperties(
                                enabled = true,
                                labels  = dateLabels
                            )


                            val points = uiState.chartData.map { it.second }
                            Log.d("CharDetailsPage", "$points")
                            val rawMin  = points.minOrNull() ?: 0.0
                            val rawMax  = points.maxOrNull() ?: 0.0

                            val range   = (rawMax - rawMin).takeIf { it != 0.0 } ?: 0.0001
                            val pad     = range * 0.05
                            val minVal  = rawMin - pad
                            val maxVal  = rawMax + pad

                            LineChart(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                data = lines,
                                minValue        = minVal,
                                maxValue        = maxVal,
                                labelProperties = labelProps,

                                animationMode = AnimationMode.Together(
                                    delayBuilder = {
                                        it * 500L
                                    }
                                )
                            )
                        } else {

                        }

                        Spacer(Modifier.height(10.dp))
                        TimeRangeSelection(selectedTimeRange, onTimeRangeSelected)
                    }
                }
            }
        }
    }
}

private fun Int.withOrdinalSuffix(): String = when {
    this % 100 in 11..13 -> "${this}th"
    this % 10 == 1       -> "${this}st"
    this % 10 == 2       -> "${this}nd"
    this % 10 == 3       -> "${this}rd"
    else                  -> "${this}th"
}







