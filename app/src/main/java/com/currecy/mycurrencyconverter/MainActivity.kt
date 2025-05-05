package com.currecy.mycurrencyconverter

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.currecy.mycurrencyconverter.navigation.AppNavGraph
import com.currecy.mycurrencyconverter.navigation.AppScreen
import com.currecy.mycurrencyconverter.ui.theme.MyCurrencyConverterTheme
import com.currecy.mycurrencyconverter.utills.ui_utills.BottomNavigationBar
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.haze.HazeState


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            var selectedScreen by remember { mutableStateOf( AppScreen.HomePage ) }
            val hazeState = remember { HazeState() }

            var takePhoto by remember { mutableStateOf(false) }

            Log.d("MainScreen", "$takePhoto")

            MyCurrencyConverterTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
                ){
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
                            AppNavGraph(
                                navController = navController,
                                hazeState = hazeState,
                                takePhoto = takePhoto
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
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 30.dp),
                        takePhoto = { newValue -> takePhoto = newValue}
                        )
                    }
                }
            }
        }
    }





