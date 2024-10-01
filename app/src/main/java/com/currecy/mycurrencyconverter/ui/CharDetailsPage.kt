package com.currecy.mycurrencyconverter.ui


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.currecy.mycurrencyconverter.TimeRange
import com.currecy.mycurrencyconverter.model.CardCurrencyViewModel
import com.currecy.mycurrencyconverter.model.DetailUiState
import com.currecy.mycurrencyconverter.model.DetailViewModel
import com.currecy.mycurrencyconverter.model.NewsViewModel
import com.currecy.mycurrencyconverter.ui.LineChart.ExchangeRateLineChart
import com.currecy.mycurrencyconverter.ui.News.NewsItem

@Composable
fun DetailScreen(
    conversionId: Int,
    navController: NavController,
    cardCurrencyViewModel: CardCurrencyViewModel = hiltViewModel(),
    detailViewModel: DetailViewModel = hiltViewModel(),
    newsViewModel: NewsViewModel = hiltViewModel()
) {

    val scrollState = rememberLazyListState()

    val conversion by cardCurrencyViewModel.getConversionById(conversionId).collectAsState(initial = null)
    val uiState by detailViewModel.uiState.collectAsState()
    val newsArticles by newsViewModel.newsArticles.collectAsState()

    LaunchedEffect(conversion?.sourceCurrency, conversion?.targetCurrency) {
        conversion?.let {
            newsViewModel.fetchNews(it.sourceCurrency, it.targetCurrency)
        }
    }

    // MutableTransitionState to control visibility
    val chartVisibilityState = remember {
        MutableTransitionState(true).apply {
            targetState = scrollState.firstVisibleItemIndex == 0 && scrollState.firstVisibleItemScrollOffset < 200
        }
    }

    // Update the visibility based on the scroll position
    LaunchedEffect(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset) {
        chartVisibilityState.targetState = scrollState.firstVisibleItemIndex == 0 && scrollState.firstVisibleItemScrollOffset < 400
    }


    // Current selected time range, default to 30 days
    var selectedTimeRange by remember { mutableStateOf(TimeRange.THIRTY_DAYS) }

    // Fetch historical rates when the screen is displayed or time range changes
    LaunchedEffect(conversion, selectedTimeRange) {
        conversion?.let {
            detailViewModel.fetchHistoricalRates(it, selectedTimeRange)
        }
    }

    Scaffold(
        content = { innerPadding ->
            if (conversion != null) {
                val updatedConversion = conversion!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding()
                        .padding(top = 5.dp, start = 7.dp, end = 7.dp)
                ) {
                    // Display Conversion Details
                    Text(
                        text = " ${updatedConversion.sourceCurrency.uppercase()} / ${updatedConversion.targetCurrency.uppercase()}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        fontSize = 35.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ChartSection(
                        uiState = uiState,
                        chartVisibilityState = chartVisibilityState,
                        selectedTimeRange = selectedTimeRange,
                        onTimeRangeSelected = { timeRange ->
                            selectedTimeRange = timeRange
                        })

                    Spacer(modifier = Modifier.height(16.dp))

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
                                        text = "No news available for ${updatedConversion.sourceCurrency.uppercase()}.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
                    else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Conversion not found", style = MaterialTheme.typography.bodyLarge)
                }
            }

        })

}


@Composable
fun ChartSection(
    uiState: DetailUiState,
    chartVisibilityState: MutableTransitionState<Boolean>,
    selectedTimeRange: TimeRange,
    onTimeRangeSelected: (TimeRange) -> Unit
) {
    AnimatedVisibility(visibleState = chartVisibilityState) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(12.dp)
                )
                .fillMaxWidth()
                .padding(5.dp)
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
                        Spacer(modifier = Modifier.height(10.dp))
                        TimeRangeSelection(
                            selectedTimeRange = selectedTimeRange,
                            onTimeRangeSelected = onTimeRangeSelected
                        )
                    }
                }
            }
        }
    }
}




// TimeRangeSelection.kt
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

                    containerColor = if (timeRange == selectedTimeRange) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface,
                )
            ) {
                Text(
                    text = when (timeRange) {
                        TimeRange.THREE_DAYS -> "3D"
                        TimeRange.SEVEN_DAYS -> "7D"
                        TimeRange.TWO_WEEKS -> "2W"
                        TimeRange.THIRTY_DAYS -> "30D"
                    },
                    color = if (timeRange == selectedTimeRange) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.inverseSurface
                )
            }
        }
    }
}







