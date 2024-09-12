package com.currecy.mycurrencyconverter

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.currecy.mycurrencyconverter.ui.theme.MyCurrencyConverterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyCurrencyConverterTheme {
                Surface{
                    MainScreen()
                }
            }
        }
    }
}


@Composable
fun MainScreen()
{

    val context = LocalContext.current

    val options = listOf("aed", "afn", "all", "amd", "ang", "aoa", "ars", "aud", "awg", "azm", "azn", "bam", "bbd", "bdt", "bef", "bgn",
        "bhd", "bif", "bmd", "bnd", "bob", "brl", "bsd", "btn", "bwp", "byn", "byr", "bzd", "cad", "cdf", "chf", "clp",
        "cnh", "cny", "cop", "crc", "cuc", "cup", "cve", "czk", "djf", "dkk", "dop", "dzd", "eek", "egp", "ern", "etb",
        "eur", "fjd", "fkp", "gbp", "gel", "ghc", "ghs", "gip", "gmd", "gnf", "gtq", "gyd", "hkd", "hnl", "hrk", "htg",
        "huf", "idr", "ils", "inr", "iqd", "irr", "isk", "itl", "jmd", "jod", "jpy", "kes", "kgs", "khr", "kmf", "kpw",
        "krw", "kwd", "kyd", "kzt", "lak", "lbp", "lsl", "ltl", "lvl", "lyd", "mad", "mdl", "mga", "mgf", "mkd", "mmk",
        "mnt", "mop", "mro", "mru", "mur", "mvr", "mwk", "mxn", "mxv", "myr", "mzn", "nad", "ngn", "nio", "nlg", "nok",
        "npr", "nzd", "omr", "pab", "pen", "pgk", "php", "pkr", "pln", "pte", "pyg", "qar", "ron", "rsd", "rub", "rwf",
        "sar", "sbd", "scr", "sdd", "sdg", "sek", "sgd", "shp", "sit", "skk", "sle", "sll", "sos", "spl", "srd", "std",
        "stn", "svc", "syp", "szl", "thb", "tjs", "tmm", "tmt", "tnd", "top", "trl", "try", "ttd", "tvd", "twd", "tzs",
        "uah", "ugx", "usd", "uyu", "uzs", "val", "veb", "ved", "vef", "ves", "vnd", "vuv", "xaf", "xag", "xau", "xcd",
        "xdr", "xof", "xpf", "yer", "zar", "zmw", "zmk", "zwd", "zwg", "zwl")


    var baseCurrency by remember { mutableStateOf("") }

    var targetCurrency by remember { mutableStateOf("") }

    var convertedAmount by remember { mutableStateOf("") }

    var amountInput by remember { mutableStateOf("") }



    LaunchedEffect(amountInput, baseCurrency, targetCurrency) {
        if (amountInput.isNotEmpty() && baseCurrency.isNotEmpty() && targetCurrency.isNotEmpty()) {
            val converter = CurrencyConverter(context)  // Use the context to create CurrencyConverter
            val amountToConvert = amountInput.toDoubleOrNull() ?: 0.0
            try {
                val convertedValue = converter.convert(baseCurrency, targetCurrency, amountToConvert)
                convertedAmount = convertedValue.toString()
                Log.d("Converted Amount", convertedAmount)
            } catch (e: Exception) {
                Log.e("Conversion Error", "Error converting: ${e.message}")
            }
        }
    }

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        ){

            Row(
                modifier = Modifier.fillMaxWidth(),
                ) {
                EditNumberField(
                    label = R.string.base_currency_input,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    modifier = Modifier
                        .width(150.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                DropdownMenuSpinner(
                    optionsList = options,
                    modifier = Modifier
                        .weight(1f)
                        .width(100.dp)
                        .height(70.dp)
                        .padding(top = 10.dp, bottom = 10.dp)
                ) {
                        currency ->
                    baseCurrency = currency
                }
            }

            Row (
                modifier = Modifier.fillMaxWidth(),
            ){
                EditNumberField(
                    label = R.string.target_currency_input,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    value = convertedAmount,
                    onValueChange = { },
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.width(5.dp))


                DropdownMenuSpinner(
                    optionsList = options,
                     modifier = Modifier
                        .weight(1f)
                        .width(100.dp)
                        .height(70.dp)
                        .padding(top = 10.dp, bottom = 10.dp)
                    ) {
                        currency ->
                    targetCurrency = currency
                }
            }
        }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuSpinner(modifier: Modifier=Modifier, optionsList: List<String>, onCurrencySelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf("") }  // Holds the text entered or selected

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        // TextField to allow user to input or select a currency
        OutlinedTextField(
            value = selectedOptionText,
            onValueChange = { input ->
                selectedOptionText = input  // Update input
                expanded = true  // Expand the dropdown when the user types
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(15.dp),
            singleLine = true,
            modifier = Modifier.menuAnchor(),
            readOnly = true
        )

        val filteringOptions = optionsList.filter {
            it.contains(selectedOptionText, ignoreCase = true)
        }

        // Show dropdown only if there are filtered options and the dropdown is expanded
        if (expanded) {
            DropdownMenu(
                modifier = Modifier
                    .background(Color.White)
                    .exposedDropdownSize(true),
                properties = PopupProperties(focusable = false),
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                optionsList.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            selectedOptionText = selectionOption  // Update the selected option
                            expanded = false  // Close the dropdown
                            onCurrencySelected(selectionOption)  // Pass selected value to MainScreen
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}

@Composable
fun EditNumberField(
        @StringRes label: Int,
        keyboardOptions: KeyboardOptions,
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        shape = RoundedCornerShape(15.dp),
        singleLine = true,
        keyboardOptions = keyboardOptions
    )
}

@Preview(
        showBackground = true,
        showSystemUi = true
)
@Composable
fun GreetingPreview() {
    MyCurrencyConverterTheme {
        MainScreen()
    }
}