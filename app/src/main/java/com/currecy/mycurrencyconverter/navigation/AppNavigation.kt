package com.currecy.mycurrencyconverter.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.currecy.mycurrencyconverter.ui.AddAndSearchChartsApp
import com.currecy.mycurrencyconverter.ui.CameraConversionScreen
import com.currecy.mycurrencyconverter.ui.MainScreenCurrencyConverterEditTextView
import dev.chrisbanes.haze.HazeState

@Composable
fun AppNavGraph(
    navController: NavHostController,
    hazeState: HazeState
) {
    NavHost(
        navController = navController,
        startDestination = "home_graph"
    ) {
        navigation(startDestination = "home_page", route = "home_graph") {
            composable("home_page") { MainScreenCurrencyConverterEditTextView(hazeState = hazeState) }
            composable("camera_conversion_page") { CameraConversionScreen(hazeState) }
            composable("search_chart_page") { AddAndSearchChartsApp(navController, hazeState = hazeState) }
        }
    }
}

object Routes {
    const val HOME_PAGE = "home_page"
    const val SEARCH_CHART_PAGE = "search_chart_page"
    const val EXCHANGE_ANALYTICS_PAGE = "exchange_analytics_page"
    const val CAMERA_CONVERSION_PAGE = "camera_conversion_page"
}


enum class AppScreen(val route: String) {
    HomePage(Routes.HOME_PAGE),
    SearchChartPage(Routes.SEARCH_CHART_PAGE),
    ExchangeAnalyticsPage(Routes.EXCHANGE_ANALYTICS_PAGE),
    CameraConversionPage(Routes.CAMERA_CONVERSION_PAGE)

}