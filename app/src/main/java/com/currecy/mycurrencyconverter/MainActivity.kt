package com.currecy.mycurrencyconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.currecy.mycurrencyconverter.database.AppDatabase
import com.currecy.mycurrencyconverter.database.CurrencyRateDao
import com.currecy.mycurrencyconverter.ui.MainScreen
import com.currecy.mycurrencyconverter.ui.theme.MyCurrencyConverterTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var currencyDao: CurrencyRateDao

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            // Use the singleton pattern to get the database instance
            val db = AppDatabase.getDatabase(applicationContext)

            currencyDao = db.currencyRateDao()

            setContent {
                MyCurrencyConverterTheme {

                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding(),
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


}


