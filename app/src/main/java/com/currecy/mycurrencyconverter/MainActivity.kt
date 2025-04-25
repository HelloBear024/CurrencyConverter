package com.currecy.mycurrencyconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.currecy.mycurrencyconverter.model.cameraModel.CameraViewModel
import com.currecy.mycurrencyconverter.model.homeModel.CurrencyViewModel
import com.currecy.mycurrencyconverter.ui.AddAndSearchChartsApp
import com.currecy.mycurrencyconverter.ui.BottomNavigation.Screen
import com.currecy.mycurrencyconverter.ui.CameraConversionScreen
import com.currecy.mycurrencyconverter.ui.DetailScreen
import com.currecy.mycurrencyconverter.ui.MainScreenCurrencyConverterEditTextView
import com.currecy.mycurrencyconverter.ui.theme.MyCurrencyConverterTheme
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.haze.HazeDefaults.blurRadius
import dev.chrisbanes.haze.HazeDefaults.noiseFactor
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
//    private lateinit var currencyDao: CurrencyRateDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val navController = rememberNavController()
            var selectedScreen by remember { mutableStateOf( AppScreen.HomePage ) }
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry.value?.destination?.route

            val hazeState = remember { HazeState() }


            MyCurrencyConverterTheme {

                Scaffold (
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) { innerPadding ->

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AuthNavGraph(
                            navController = navController,
                            hazeState = hazeState
                        )
                    }
                }
                BottomNavigationBar(
                    navController = navController,
                    selectedScreen = selectedScreen,
                    onScreenSelected = { screen ->
                        if (selectedScreen != screen) {
                            selectedScreen = screen
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    hazeState = hazeState,
                    modifier = Modifier
//                                .align(Alignment.BottomCenter)
                        .padding(bottom = 30.dp)
//                                .zIndex(3f)
                )
            }
        }
    }
}





@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var selectedScreen by remember { mutableStateOf(AppScreen.HomePage) }
    Box {
        Scaffold(
            content = { innerPadding ->
//                    Box(modifier = Modifier.padding(innerPadding)) {
//                        NavHost(
//                            navController = navController,
//                            startDestination = AppScreen.ConversionTextView.name
//                        ) {
//                            composable(AppScreen.ConversionTextView.name) {
//                                val currencyViewModel: CurrencyViewModel = hiltViewModel()
//                                MainScreenCurrencyConverterEditTextView(currencyViewModel = currencyViewModel)
//                            }
//                            composable(AppScreen.ConversionCamera.name) {
//                                val cameraViewModel: CameraViewModel = hiltViewModel()
//                                CameraConversionScreen(cameraViewModel = cameraViewModel)
//                            }
//                            composable(AppScreen.Charts.name) {
//                                AddAndSearchChartsApp(
//                                    navController = navController)
//                            }
//                            composable(
//                                route = "detail/{id}",
//                                arguments = listOf(navArgument("id") { type = NavType.IntType})
//                            ) {
//                                backStackEntry ->
//                                    val id = backStackEntry.arguments?.getInt("id") ?: 0
//                                DetailScreen(
//                                    conversionId = id,
//                                    navController =  navController,
//                                )
//                            }
//                        }
//                    }
            }
        )

        val fabScale by animateFloatAsState(
            targetValue = if (selectedScreen == AppScreen.CameraConversionPage) 1.2f else 1f
        )

        val fabOffsetY by animateDpAsState(
            targetValue = if (selectedScreen == AppScreen.CameraConversionPage) (-37).dp else (-42).dp
        )


        FloatingActionButton(
            onClick = {
                selectedScreen = AppScreen.CameraConversionPage
                navController.navigate(AppScreen.CameraConversionPage.name)
            },
            containerColor = if (selectedScreen == AppScreen.CameraConversionPage)
                MaterialTheme.colorScheme.onTertiary
            else
                MaterialTheme.colorScheme.tertiary,



            contentColor = if (selectedScreen == AppScreen.CameraConversionPage)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.outlineVariant,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = fabOffsetY)
                .size(65.dp * fabScale) // Adjust offset to position FAB correctly
        ) {
            Icon(
                painter = painterResource(id = R.drawable.photo),
                contentDescription = "Camera",
                modifier = Modifier.size(40.dp * fabScale)
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    navBarHeight: Dp = 70.dp,
    selectedScreen: AppScreen = AppScreen.HomePage,
    onScreenSelected: (AppScreen) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {

    val iconSize = when {
        navBarHeight < 70.dp -> 24.dp
        navBarHeight < 90.dp -> 28.dp
        else -> 32.dp
    }
    val fabSize = 75.dp
    val fabMargin = 8.dp
    val cutoutRadius = with(LocalDensity.current) { (fabSize / 2 + fabMargin).toPx() }
    val cornerRadius = with(LocalDensity.current) { 30.dp.toPx()}

    Surface(
        shape = Screen(cutoutRadius, cornerRadius),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp)
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.ultraThin(containerColor = MaterialTheme.colorScheme.primary)
            ){
                blurRadius = 30.dp
                noiseFactor
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            NavigationIcon(
                isSelected = selectedScreen == AppScreen.HomePage,
                onClick = {
                    if (selectedScreen != AppScreen.HomePage) {
                        onScreenSelected(AppScreen.HomePage)
                    }
                },
                icon = Icons.Default.EuroSymbol,
                iconSize = iconSize,
                contentDescription = "Home",
                defaultTint = MaterialTheme.colorScheme.inverseSurface,
                selectedTint = MaterialTheme.colorScheme.tertiary // Adjust as needed
            )

            Spacer(modifier = Modifier.weight(3f))

            // Charts Icon
            NavigationIcon(
                isSelected = selectedScreen == AppScreen.SearchChartPage,
                onClick = {
                    if (selectedScreen != AppScreen.SearchChartPage) {
                        onScreenSelected(AppScreen.SearchChartPage)
                    }                    },
                icon = Icons.Default.Analytics,
                iconSize = iconSize,
                contentDescription = "Charts",
                defaultTint = MaterialTheme.colorScheme.inverseSurface,
                selectedTint = MaterialTheme.colorScheme.tertiary // Adjust as needed
            )
            Spacer(modifier = Modifier.weight(1f))

        }
    }
}





@Composable
fun NavigationIcon(
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    iconSize: Dp,
    defaultTint: Color,
    selectedTint: Color
) {
    val size by animateDpAsState(
        targetValue = if (isSelected) iconSize * 1.2f else iconSize
    )
    val tint by animateColorAsState(
        targetValue = if (isSelected) selectedTint else defaultTint
    )

    IconButton(
        onClick = onClick,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size)
        )
    }
}



@Composable
fun AuthNavGraph(
    navController: NavHostController,
    hazeState: HazeState
) {
    NavHost(
        navController = navController,
        startDestination = "home_page"
    ) {
        navigation(startDestination = "home_page", route = "home_graph") {
            composable("home_page") { MainScreenCurrencyConverterEditTextView(hazeState = hazeState) }
            composable("home_page") { CameraConversionScreen() }
            composable("past_activity_log"){ AddAndSearchChartsApp(hazeState = hazeState, navController = navController )}
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




