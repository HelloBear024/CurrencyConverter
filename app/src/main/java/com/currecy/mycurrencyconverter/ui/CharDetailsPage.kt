package com.currecy.mycurrencyconverter.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.currecy.mycurrencyconverter.model.CardCurrencyViewModel

@Composable
fun DetailScreen(
    conversionId: Int,
    cardCurrencyViewModel: CardCurrencyViewModel
) {


    val conversion by cardCurrencyViewModel.getConversionById(conversionId).collectAsState(initial = null)

    Scaffold(
        content = { innerPadding ->
            if (conversion != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    Text(text = "From: ${conversion!!.sourceCurrency}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "To: ${conversion!!.targetCurrency}", style = MaterialTheme.typography.bodySmall)
                    // Add more details as needed
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Conversion not found", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    )
}
