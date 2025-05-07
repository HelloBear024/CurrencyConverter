package com.currecy.mycurrencyconverter

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.currecy.mycurrencyconverter.navigation.AppNavGraph
import com.currecy.mycurrencyconverter.navigation.AppScreen
import com.currecy.mycurrencyconverter.ui.theme.MyCurrencyConverterTheme
import com.currecy.mycurrencyconverter.utills.ui_utills.BottomNavigationBar
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT,
            ),
        )

        var showCustomSplash by mutableStateOf(true)

        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            var selectedScreen by remember { mutableStateOf(AppScreen.HomePage) }

            val navBarHazeState = remember { HazeState() }
            val inputHazeState = remember { HazeState() }

            var takePhoto by remember { mutableStateOf(false) }

            Log.d("MainScreen", "$takePhoto")

            //would be a good implimentation for the future
//            splash.setKeepOnScreenCondition { myViewModel.isLoading.value }


            MyCurrencyConverterTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {


                    if (showCustomSplash) {
                        SplashAnimation {
                            showCustomSplash = false
                        }
                    } else {

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) { innerPadding ->

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding()
                            ) {
                                AppNavGraph(
                                    navController = navController,
                                    hazeState = inputHazeState,
                                    takePhoto = takePhoto
                                )




                            BottomNavigationBar(
                                navController = navController,
                                selectedScreen = selectedScreen,
                                onScreenSelected = { screen ->
                                    if (selectedScreen != screen) {
                                        selectedScreen = screen
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                hazeState = navBarHazeState,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 30.dp),
                                takePhoto = { newValue -> takePhoto = newValue }
                            )
                        }
                    }
                }
                    }
            }
        }
    }
}



@Composable
fun SplashAnimation(onFinished: () -> Unit) {
    val logo = remember { Animatable(0f) }
    val box1OffsetY = remember { Animatable(0f) }
    val box2OffsetY = remember { Animatable(0f) }
    val finalImageAlpha = remember { Animatable(0f) }


    LaunchedEffect(Unit) {
        val phase1Duration = 1000
        launch {
            box1OffsetY.animateTo(
                targetValue = -50f,
                animationSpec = tween(durationMillis = phase1Duration, easing = FastOutSlowInEasing)
            )
        }
        launch {
            box2OffsetY.animateTo(
                targetValue = 50f,
                animationSpec = tween(durationMillis = phase1Duration, easing = FastOutSlowInEasing)
            )
        }
        delay(phase1Duration.toLong())

        val phase2Duration = 3000

        finalImageAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = phase2Duration, easing = FastOutSlowInEasing)
        )

        onFinished()

    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F1F1F)),
        contentAlignment = Alignment.Center
    ) {


        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.dollar_sign),
                contentDescription = null,
                modifier = Modifier
                    .size(82.dp)
                    .alpha(finalImageAlpha.value)
//                    .offset(x = box2OffsetY.value.dp)
            )

            Column {

                Text(
                    text = "XRate Pro",
                    fontSize = 32.sp,
                    color = Color(0xFFFD5B66),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(bottom = 50.dp)
                        .offset(y = box2OffsetY.value.dp)
                )

                Text(
                    text = "Currencies in one view",
                    fontSize = 20.sp,
                    color = Color(0xFFFD5B66),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(top = 50.dp)
                        .offset(y = box1OffsetY.value.dp)
                )

            }
        }
    }
}



