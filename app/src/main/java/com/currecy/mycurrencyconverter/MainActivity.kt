package com.currecy.mycurrencyconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.currecy.mycurrencyconverter.model.cameraModel.CameraViewModel
import com.currecy.mycurrencyconverter.model.homeModel.CurrencyViewModel
import com.currecy.mycurrencyconverter.ui.AddAndSearchChartsApp
import com.currecy.mycurrencyconverter.ui.CameraConversionScreen
import com.currecy.mycurrencyconverter.ui.DetailScreen
import com.currecy.mycurrencyconverter.ui.MainScreenCurrencyConverterEditTextView
import com.currecy.mycurrencyconverter.ui.BottomNavigation.Screen
import com.currecy.mycurrencyconverter.ui.theme.MyCurrencyConverterTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
//    private lateinit var currencyDao: CurrencyRateDao

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
                MyCurrencyConverterTheme {

                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {

                        MainScreen(
                        )
                    }
                }
            }
        }


    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }



    @Composable
    fun MainScreen() {
        val navController = rememberNavController()
        var selectedScreen by remember { mutableStateOf(AppScreen.ConversionTextView) }

//        TestCurrencyDao(currencyDao)
        Box {
            Scaffold(
                topBar = { TopAppBar() },
                bottomBar = {
                    BottomNavigationBar(
                        navController = navController,
                        selectedScreen = selectedScreen,
                        onScreenSelected = { screen ->
                            selectedScreen = screen
                            navController.navigate(screen.name)
                        }
                    )
                },
                content = { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = AppScreen.ConversionTextView.name
                        ) {
                            composable(AppScreen.ConversionTextView.name) {
                                val currencyViewModel: CurrencyViewModel = hiltViewModel()
                                MainScreenCurrencyConverterEditTextView(currencyViewModel = currencyViewModel)
                            }
                            composable(AppScreen.ConversionCamera.name) {
                                val cameraViewModel: CameraViewModel = hiltViewModel()
                                CameraConversionScreen(cameraViewModel = cameraViewModel)
                            }
                            composable(AppScreen.Charts.name) {
                                AddAndSearchChartsApp(
                                    navController = navController)
                            }
                            composable(
                                route = "detail/{id}",
                                arguments = listOf(navArgument("id") { type = NavType.IntType})
                            ) {
                                backStackEntry ->
                                    val id = backStackEntry.arguments?.getInt("id") ?: 0
                                DetailScreen(
                                    conversionId = id,
                                    navController =  navController,
                                )
                            }
                        }
                    }
                }
            )

            val fabScale by animateFloatAsState(
                targetValue = if (selectedScreen == AppScreen.ConversionCamera) 1.2f else 1f
            )

            val fabOffsetY by animateDpAsState(
                targetValue = if (selectedScreen == AppScreen.ConversionCamera) (-37).dp else (-42).dp
            )


            FloatingActionButton(
                onClick = {
                    selectedScreen = AppScreen.ConversionCamera
                    navController.navigate(AppScreen.ConversionCamera.name)
                },
                containerColor = if (selectedScreen == AppScreen.ConversionCamera)
                    MaterialTheme.colorScheme.onTertiary
                else
                    MaterialTheme.colorScheme.tertiary,



                contentColor = if (selectedScreen == AppScreen.ConversionCamera)
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


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopAppBar(modifier: Modifier = Modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomEnd = 20.dp , bottomStart = 20.dp))

        ) {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            modifier = Modifier
                                .size(60.dp)
                                .padding(8.dp),
                            painter = painterResource(R.drawable.currency_exchange),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                            contentDescription = null
                        )

                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                modifier = modifier.fillMaxWidth(),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
            )
        }
    }


    @Composable
    fun BottomNavigationBar(
        navController: NavHostController,
        selectedScreen: AppScreen,
        onScreenSelected: (AppScreen) -> Unit
    ) {
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
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 15.dp, horizontal = 15.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))

                NavigationIcon(
                    isSelected = selectedScreen == AppScreen.ConversionTextView,
                    onClick = {
                        onScreenSelected(AppScreen.ConversionTextView)
                    },
                    painter = painterResource(R.drawable.home),
                    contentDescription = "Home",
                    defaultTint = MaterialTheme.colorScheme.inverseSurface,
                    selectedTint = MaterialTheme.colorScheme.tertiary // Adjust as needed
                )

                Spacer(modifier = Modifier.weight(3f))

                // Charts Icon
                NavigationIcon(
                    isSelected = selectedScreen == AppScreen.Charts,
                    onClick = {
                        onScreenSelected(AppScreen.Charts)
                    },
                    painter = painterResource(id = R.drawable.monitor),
                    contentDescription = "Charts",
                    defaultTint = MaterialTheme.colorScheme.inverseSurface,
                    selectedTint = MaterialTheme.colorScheme.tertiary // Adjust as needed
                )
                Spacer(modifier = Modifier.weight(1f))

            }
            }
        }
    }



    @Composable
    fun NavigationIcon(
        isSelected: Boolean,
        onClick: () -> Unit,
        painter: Painter,
        contentDescription: String,
        defaultTint: Color,
        selectedTint: Color
    ) {
        val size by animateDpAsState(
            targetValue = if (isSelected) 48.dp else 40.dp
        )
        val tint by animateColorAsState(
            targetValue = if (isSelected) selectedTint else defaultTint
        )

        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(size)
        ) {
            Icon(
                painter = painter,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(size)
            )
        }
    }

    enum class AppScreen() {
        ConversionTextView,
        ConversionCamera,
        Charts,
        Details
    }




