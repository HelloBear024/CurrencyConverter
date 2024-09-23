package com.currecy.mycurrencyconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.currecy.mycurrencyconverter.database.AppDatabase
import com.currecy.mycurrencyconverter.database.CurrencyRateDao
import com.currecy.mycurrencyconverter.ui.CameraConversionScreen
import com.currecy.mycurrencyconverter.ui.MainScreenCurrencyConverterEditTextView
import com.currecy.mycurrencyconverter.ui.Screen
import com.currecy.mycurrencyconverter.ui.theme.MyCurrencyConverterTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var currencyDao: CurrencyRateDao

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)

            currencyDao = db.currencyRateDao()

            setContent {
                MyCurrencyConverterTheme {

                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        MainScreen(currencyDao = currencyDao)
                    }
                }
            }
        }
    }

    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }



    @Composable
    fun MainScreen(currencyDao: CurrencyRateDao) {
        val navController = rememberNavController()
        Box {
            Scaffold(
                topBar = { TopAppBar() },
                bottomBar = {
                    BottomNavigationBar(navController = navController)
                },
                content = { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = AppScreen.ConversionTextView.name
                        ) {
                            composable(AppScreen.ConversionTextView.name) {
                                MainScreenCurrencyConverterEditTextView(currencyDao = currencyDao)
                            }
                            composable(AppScreen.ConversionCamera.name) {
                                CameraConversionScreen(currencyDao = currencyDao)
                            }
                            composable(AppScreen.Charts.name) {
                                // ChartsScreen()
                            }
                        }
                    }
                }
            )

            // Place FAB overlapping the BottomAppBar
            FloatingActionButton(
                onClick = { navController.navigate(AppScreen.ConversionCamera.name) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-28).dp) // Adjust offset to position FAB correctly
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.photo),
                    contentDescription = "Camera"
                )
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopAppBar(modifier: Modifier = Modifier) {
        CenterAlignedTopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier
                            .size(64.dp)
                            .padding(8.dp),
                        painter = painterResource(R.drawable.exchange),
                        contentDescription = null )

                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            },
            modifier = modifier
        )
    }


//    @Composable
//    fun BottomNavigationBar(navController: NavHostController) {
//        BottomAppBar(
//            containerColor = MaterialTheme.colorScheme.primaryContainer,
//            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
//        ) {
//            // Left IconButton
//            IconButton(
//                onClick = { navController.navigate(AppScreen.ConversionTextView.name) },
//                modifier = Modifier.weight(1f)
//            ) {
//                Icon(
//                    painter = painterResource(R.drawable.home),
//                    contentDescription = "Home",
//                    tint = MaterialTheme.colorScheme.primary
//                )
//            }
//
//            Spacer(modifier = Modifier.weight(1f)) // This spacer centers the FAB
//
//            // Right IconButton
//            IconButton(
//                onClick = { navController.navigate(AppScreen.Charts.name) },
//                modifier = Modifier.weight(1f)
//            ) {
//                Icon(
//                    painter = painterResource(id = R.drawable.monitor),
//                    contentDescription = "Charts",
//                    tint = MaterialTheme.colorScheme.surfaceTint
//                )
//            }
//        }
//    }

    @Composable
    fun BottomNavigationBar(navController: NavHostController) {
        val fabSize = 56.dp // Default FAB size
        val fabMargin = 8.dp // Margin between FAB and BottomAppBar
        val cutoutRadius = with(LocalDensity.current) { (fabSize / 2 + fabMargin).toPx() }

        Surface(
            shape = Screen(cutoutRadius),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
//            elevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp) // Adjust height as needed
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clip(
                    RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .padding(vertical = 15.dp, horizontal = 15.dp)
            ) {
                IconButton(
                    onClick = { navController.navigate(AppScreen.ConversionTextView.name) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.home),
                        contentDescription = "Home",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { navController.navigate(AppScreen.Charts.name) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.monitor),
                        contentDescription = "Charts",
                        tint = MaterialTheme.colorScheme.surfaceTint
                    )
                }
            }
        }
    }




//    @Composable
//    fun BottomNavigationBar(navController: NavHostController, modifier: Modifier = Modifier) {
//
//        val currentBackStackEntry by navController.currentBackStackEntryAsState()  // Get current back stack
//        val currentDestination = currentBackStackEntry?.destination
//
//        var navNum by remember {
//            mutableStateOf(0)
//        }
//
//        Row(
//            modifier = modifier
//                .fillMaxWidth()
//                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
//                .background(MaterialTheme.colorScheme.primaryContainer)
//                .padding(vertical = 15.dp, horizontal = 15.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Spacer(modifier = Modifier.weight(1f))
//
//            if (navNum == 0) {
//                IconButton(
//                    onClick = { navController.navigate(AppScreen.ConversionTextView.name) },
//                    modifier = Modifier.size(45.dp)
//                ) {
//                    Icon(
//                        painter = painterResource(R.drawable.home),
//                        contentDescription = "home",
//                        tint = MaterialTheme.colorScheme.primary,
//                        modifier = Modifier
//                            .size(45.dp)
//                    )
//                }
//            } else {
//                IconButton(onClick = { navNum = 0 }) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.home),
//                        contentDescription = "home",
//                        tint = MaterialTheme.colorScheme.primary,
//                        modifier = Modifier
//                            .size(25.dp)
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.weight(1f))
//
//
//            if (navNum == 1) {
//
//                IconButton(onClick = { navController.navigate(AppScreen.ConversionCamera.name) }) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.photo),
//                        contentDescription = "home",
//                        tint = MaterialTheme.colorScheme.secondary,
//                        modifier = Modifier.size(25.dp)
//                    )
//                }
//            } else {
//                IconButton(onClick = { navNum = 1 }) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.photo),
//                        contentDescription = "home",
//                        tint = MaterialTheme.colorScheme.secondary,
//                        modifier = Modifier.size(45.dp)
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.weight(1f))
//
//            if (navNum == 2) {
//
//                IconButton(onClick = { /*TODO*/ }) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.monitor),
//                        contentDescription = "home",
//                        tint = MaterialTheme.colorScheme.secondary,
//                        modifier = Modifier.size(25.dp)
//                    )
//                }
//            } else {
//                IconButton(onClick = { navNum = 2 }) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.monitor),
//                        contentDescription = "home",
//                        tint = MaterialTheme.colorScheme.surfaceTint,
//                        modifier = Modifier.size(45.dp)
//                    )
//                }
//            }
//            Spacer(modifier = Modifier.weight(1f))
//        }
//    }

    enum class AppScreen() {
        ConversionTextView,
        ConversionCamera,
        Charts
    }
}



