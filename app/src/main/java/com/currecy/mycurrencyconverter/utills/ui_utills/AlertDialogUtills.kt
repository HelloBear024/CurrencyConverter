package com.currecy.mycurrencyconverter.utills.ui_utills


import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.currecy.mycurrencyconverter.utills.ui_size_params.alertDialogHeaderFontSize
import com.currecy.mycurrencyconverter.utills.ui_size_params.alertDialogTextBottomPadding
import com.currecy.mycurrencyconverter.utills.ui_size_params.alertDialogTextStartPadding
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConversionDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (String, String) -> Unit,
    optionsList: List<Pair<String, String>>,
    initialSourceCurrency: String = optionsList[0].second,
    initialTargetCurrency: String = optionsList[1].second,
    hazeState: HazeState,
) {

    var sourceCurrency by remember { mutableStateOf(initialSourceCurrency) }
    var targetCurrency by remember { mutableStateOf(initialTargetCurrency) }

    LaunchedEffect(sourceCurrency, targetCurrency) {
        Log.d("CurrencyConversionDialog", "Selected source: $sourceCurrency, target: $targetCurrency")
    }

    AlertDialog(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.ultraThin()
            ) {
                blurRadius = 50.dp
            },
        containerColor = Color.Transparent,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            IconButton(onClick = { onConfirm(sourceCurrency, targetCurrency) }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Save Program",
                    tint = Color(0xFFFD5B66),
                    modifier = Modifier.size(56.dp)
                )
            }
        },
        title = {
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = "Saving Program",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .padding(
                            bottom = alertDialogTextBottomPadding(),
                            start = alertDialogTextStartPadding()
                        ),
                    fontSize = alertDialogHeaderFontSize(),
                    color = Color(0xFFFD5B66),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = "From:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(start = 6.dp, bottom = 8.dp)
                )

                DropdownMenuSpinner(
                    optionsList = optionsList,
                    selectedCurrency = sourceCurrency,
                    onCurrencySelected = {
                        sourceCurrency = it
                        Log.d("CurrencyConversionDialog", "Source currency selected: $it")

                    },
                    modifier = Modifier.width(160.dp),
                    backgroundColor = Color(0xFFFD5B66),
                    textColor = Color.White,
                    trailingColor = Color.White,
                    borderColor = Color.Transparent,

                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "To:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(start = 6.dp, bottom = 8.dp)
                )

                DropdownMenuSpinner(
                    optionsList = optionsList,
                    selectedCurrency = targetCurrency,
                    onCurrencySelected = { targetCurrency = it },
                    modifier = Modifier.width(160.dp),
                    backgroundColor = Color(0xFFFD5B66),
                    textColor = Color.White,
                    trailingColor = Color.White,
                    borderColor = Color.Transparent,
                )
            }
        }
    )
}

