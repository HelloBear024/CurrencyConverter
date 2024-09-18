package com.currecy.mycurrencyconverter.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    fun MainScreen( currencyDao : CurrencyRateDao) {
    val currencyViewModel: CurrencyViewModel = viewModel(
        factory = CurrencyViewModelFactory(currencyDao)
    )

    val currencyItems = remember { mutableStateListOf(
        "" to "",  // First item
        "" to ""  ) } // Store pairs of base and target currencies

    // Add an initial CurrencySelectorItem
    if (currencyItems.isEmpty()) {
        currencyItems.add("" to "")
    }

    val converterUIState by currencyViewModel.currencyRatesState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar()
        },
        floatingActionButton = {
            if (converterUIState.numberOfItems < 6) {  // Check if number of items is less than 6
                AddMoreContainersBtn(onClick = {
                    // Add more items dynamically
                    currencyViewModel.addMoreItems()
                })
            }
        },

        bottomBar = {
            BottomBar()
        },
        modifier = Modifier.fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars)

    ) { innerPadding ->



            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                items(converterUIState.numberOfItems) { index ->
                    val (amount, currency) = when (index) {
                        0 -> converterUIState.firstValue to converterUIState.firstCurrency
                        1 -> converterUIState.secondValue to converterUIState.secondCurrency
                        2 -> converterUIState.thirdValue to converterUIState.thirdCurrency
                        3 -> converterUIState.fourthValue to converterUIState.fourthCurrency
                        4 -> converterUIState.fifthValue to converterUIState.fifthCurrency
                        5 -> converterUIState.sixthValue to converterUIState.sixthCurrency
                        else -> 0.0 to "" // Fallback case
                    }

                    CurrencySelectorItem(
                        currencyOptions = CurrencyOptionsData.options,
                        value = if (amount == 0.0) "" else amount.toString(), // Amount value
                        onAmountChange = {  newAmountString ->
                            val newAmount = newAmountString.toDoubleOrNull() ?: 0.0
                            currencyViewModel.onAmountChange(newAmount, index)
                        },
                        onCurrencyChange = { newCurrency ->
                            currencyViewModel.onCurrencyChange(newCurrency, index)
                        },
                        label = R.string.base_currency_input,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        )
                    )




//                itemsIndexed(listOf(
//                    converterUIState.firstValue to converterUIState.firstCurrency,
//                    converterUIState.secondValue to converterUIState.secondCurrency,
//                    converterUIState.thirdValue to converterUIState.thirdCurrency,
//                    converterUIState.fourthValue to converterUIState.fourthCurrency,
//                    converterUIState.fifthValue to converterUIState.fifthCurrency,
//                    converterUIState.sixthValue to converterUIState.sixthCurrency
//                )) { index, (amount, currency) ->
//
//                    CurrencySelectorItem(
//                        currencyOptions = CurrencyOptionsData.options,
//                        value = amount.toString(),
//                        onAmountChange = { newAmount ->
//                            currencyViewModel.onAmountChange(newAmount.toDouble(), index)
//                        },
//                        onCurrencyChange = { newCurrency ->
//                            currencyViewModel.onCurrencyChange(newCurrency, index)
//                        },
//                        label = R.string.base_currency_input,
//                        keyboardOptions = KeyboardOptions.Default.copy(
//                            keyboardType = KeyboardType.Number,
//                            imeAction = ImeAction.Done
//                        )
//                    )


                }

            }
        }
    }





@Composable
fun CurrencySelectorItem(
    currencyOptions: List<String>,
    value: String,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    @StringRes label: Int,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp),
    ) {
        EditNumberField(
            label = label,
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
            selectedCurrency = currencyOptions[1],
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
    fun DropdownMenuSpinner(modifier: Modifier = Modifier, optionsList: List<String>,selectedCurrency: String, onCurrencySelected: (String) -> Unit) {
        var expanded by remember { mutableStateOf(false) }
        var selectedOptionText by remember { mutableStateOf(selectedCurrency ) }  // Holds the text entered or selected

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

@Composable
fun AddMoreContainersBtn(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = { onClick() },
        modifier = Modifier.padding(16.dp)

    ){
        Icon(Icons.Filled.Add, contentDescription = "Add new currency field")
    }
}

@Composable
fun BottomBar(modifier: Modifier = Modifier) {

    var navNum by remember {
        mutableStateOf(0)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(vertical = 15.dp, horizontal = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))

            if (navNum == 0) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        painter = painterResource(R.drawable.home),
                        contentDescription = "home",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(45.dp)
                    )
                }

            } else {
                IconButton(onClick = { navNum = 0 }) {
                    Icon(
                        painter = painterResource(id = R.drawable.home),
                        contentDescription = "home",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(25.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))


                if (navNum == 1) {

                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.photo),
                            contentDescription = "home",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                } else {
                    IconButton(onClick = { navNum = 1 }) {
                        Icon(
                            painter = painterResource(id = R.drawable.photo),
                            contentDescription = "home",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(45.dp)
                        )
                    }
                }

            Spacer(modifier = Modifier.weight(1f))

                if (navNum == 2) {

                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.monitor),
                            contentDescription = "home",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                } else {
                    IconButton(onClick = { navNum = 2 }) {
                        Icon(
                            painter = painterResource(id = R.drawable.monitor),
                            contentDescription = "home",
                            tint = MaterialTheme.colorScheme.surfaceTint,
                            modifier = Modifier.size(45.dp)
                        )
                    }
                }

            Spacer(modifier = Modifier.weight(1f))
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
