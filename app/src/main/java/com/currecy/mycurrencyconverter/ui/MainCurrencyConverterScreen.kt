package com.currecy.mycurrencyconverter.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.currecy.mycurrencyconverter.R
import com.currecy.mycurrencyconverter.data.CurrencyOptionsData
import com.currecy.mycurrencyconverter.database.CurrencyRateDao
import com.currecy.mycurrencyconverter.model.CurrencyViewModel
import com.currecy.mycurrencyconverter.model.CurrencyViewModelFactory
import com.currecy.mycurrencyconverter.ui.theme.MyCurrencyConverterTheme


@Composable
    fun MainScreenCurrencyConverterEditTextView( currencyDao : CurrencyRateDao) {
    val currencyViewModel: CurrencyViewModel = viewModel(
        factory = CurrencyViewModelFactory(currencyDao)
    )

    val converterUIState by currencyViewModel.currencyRatesState.collectAsState()

    Scaffold(
        floatingActionButton = {
            if (converterUIState.numberOfItems < 6) {
                AddMoreContainersBtn(onClick = {
                    currencyViewModel.addMoreItems()
                })
            }
        },
        modifier = Modifier
            .fillMaxSize()

    ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(converterUIState.numberOfItems) { index ->
                    CurrencySelectorItem(
                        currencyOptions = CurrencyOptionsData.options,
                        value = if (converterUIState.values[index] == 0.0) "" else  converterUIState.values[index].toString() ,
                        onAmountChange = { newAmount ->
                            currencyViewModel.onAmountChange(newAmount.toDouble(), index)
                        },
                        onCurrencyChange = { newCurrency ->
                            currencyViewModel.onCurrencyChange(newCurrency, index)
                        },
                        selectedCurrency = converterUIState.currencies[index],
                        label = R.string.base_currency_input,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        )
                    )
                }
            }
        }
    }





@Composable
fun CurrencySelectorItem(
    currencyOptions: List<Pair<String, String>>,
    value: String,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    selectedCurrency: String,
    @StringRes label: Int,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp),
    ) {
        EditNumberField(
            label = selectedCurrency,
            keyboardOptions = keyboardOptions,
            value = value,
            onValueChange = onAmountChange,
            modifier = Modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.width(5.dp))

        DropdownMenuSpinner(
            optionsList = currencyOptions,
            selectedCurrency = selectedCurrency,
            modifier = Modifier
                .width(100.dp)
                .height(70.dp)
                .padding(top = 10.dp, bottom = 10.dp)
        ) { currency ->
            onCurrencyChange(currency)
        }
    }
}




    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
 fun DropdownMenuSpinner(
        modifier: Modifier = Modifier,
        optionsList: List<Pair<String, String>>,
        selectedCurrency: String,
        onCurrencySelected: (String) -> Unit) {

        var expanded by remember { mutableStateOf(false) }
        var selectedOptionText by remember {
            mutableStateOf(
                optionsList.find { it.second == selectedCurrency }?.second ?: optionsList[0].second
            )
        }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = modifier
        ) {

            OutlinedTextField(
                value = selectedOptionText,
                onValueChange = {},
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

            if (expanded) {
                DropdownMenu(
                    modifier = Modifier
                        .background(Color.White)
                        .exposedDropdownSize(false),
                    properties = PopupProperties(focusable = false),
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    optionsList.forEach { currencyPair ->
                        DropdownMenuItem(
                            text = { Text(currencyPair.first + " (" + currencyPair.second.uppercase() + " )") },
                            onClick = {
                                selectedOptionText =
                                    currencyPair.second
                                expanded = false
                                onCurrencySelected(currencyPair.second)
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
        label: String,
        keyboardOptions: KeyboardOptions,
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier
    ) {

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            shape = RoundedCornerShape(15.dp),
            singleLine = true,
            keyboardOptions = keyboardOptions
        )
    }

@Composable
fun AddMoreContainersBtn(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = { onClick() },
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier.padding(16.dp).size(75.dp)
    ){
        Icon(
            Icons.Filled.Add,
            contentDescription = "Add new currency field",
            modifier = Modifier.size(40.dp)
        )

    }
}


    @Preview(
        showBackground = true,
        showSystemUi = true
    )
    @Composable
    fun GreetingPreview() {
        MyCurrencyConverterTheme {

        }
    }




