package com.currecy.mycurrencyconverter.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.currecy.mycurrencyconverter.ui.AddAndSearchChartsApp
import com.currecy.mycurrencyconverter.ui.CameraPreviewScreen
import com.currecy.mycurrencyconverter.ui.DetailScreen
import com.currecy.mycurrencyconverter.ui.ImageConversionScreen
import com.currecy.mycurrencyconverter.ui.MainScreenCurrencyConverterEditTextView
import dev.chrisbanes.haze.HazeState

@Composable
fun AppNavGraph(
    navController: NavHostController,
    hazeState: HazeState,
    takePhoto: Boolean,
) {
    NavHost(
        navController = navController,
        startDestination = "home_graph"
    ) {
        navigation(startDestination = "home_page", route = "home_graph") {
            composable("home_page") { MainScreenCurrencyConverterEditTextView(hazeState = hazeState) }
            composable("camera_conversion_page") { CameraPreviewScreen(
                hazeState = hazeState,
                navController = navController,
                takePhoto = takePhoto
            ) }
            composable("search_chart_page") { AddAndSearchChartsApp(navController, hazeState = hazeState) }
            composable("image_conversion_page?uri={uri}",
                arguments = listOf(
                    navArgument("uri") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                )
            ) { navBackStackEntry ->
                val uri = navBackStackEntry.arguments?.getString("uri")

                ImageConversionScreen(
                    hazeState = hazeState,
                    imageUri = Uri.parse(uri),
                )
            }
            composable("chart_page?id={id}",
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                )
            ) { navBackStackEntry ->
                val id = navBackStackEntry.arguments?.getString("id")

                DetailScreen(
                    conversionId = id ?: "0",
                    navController = navController,
                    hazeState = hazeState
                )
            }
        }
    }
}

object Routes {
    const val HOME_PAGE = "home_page"
    const val SEARCH_CHART_PAGE = "search_chart_page"
    const val EXCHANGE_ANALYTICS_PAGE = "exchange_analytics_page"
    const val CAMERA_CONVERSION_PAGE = "camera_conversion_page"
    const val IMAGE_CONVERSION_PAGE = "image_conversion_page"
}


enum class AppScreen(val route: String) {
    HomePage(Routes.HOME_PAGE),
    SearchChartPage(Routes.SEARCH_CHART_PAGE),
    ExchangeAnalyticsPage(Routes.EXCHANGE_ANALYTICS_PAGE),
    CameraConversionPage(Routes.CAMERA_CONVERSION_PAGE),
    ImageConversionPage(Routes.IMAGE_CONVERSION_PAGE)

}