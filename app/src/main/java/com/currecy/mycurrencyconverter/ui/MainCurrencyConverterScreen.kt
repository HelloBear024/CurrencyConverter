package com.currecy.mycurrencyconverter.ui

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.currecy.mycurrencyconverter.R
import com.currecy.mycurrencyconverter.data.CurrencyOptionsData
import com.currecy.mycurrencyconverter.model.homeModel.CurrencyViewModel
import com.currecy.mycurrencyconverter.ui.theme.MyCurrencyConverterTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@Composable
fun MainScreenCurrencyConverterEditTextView(
    currencyViewModel: CurrencyViewModel = hiltViewModel()
) {
    val converterUIState by currencyViewModel.currencyRatesState.collectAsState()
    val listState = rememberLazyListState()
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        floatingActionButton = {
            if (converterUIState.numberOfItems < 6) {
                AddMoreContainersBtn(onClick = {
                    currencyViewModel.addMoreItems()
                })
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Use 'items' with a count and a key based on index
            items(
                count = converterUIState.numberOfItems,
                key = { index -> index }
            ) { index ->
                // Determine if swiping is allowed
                val canSwipe = converterUIState.numberOfItems > 2

                if (canSwipe) {
                    // State variables for swipe handling
                    var isSwiped by remember { mutableStateOf(false) }
                    val offsetX = remember { Animatable(0f) }
                    val scope = rememberCoroutineScope()

                    if (!isSwiped) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .background(Color.Transparent)
                                .pointerInput(index) {
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            scope.launch {
                                                val swipeThreshold = 150f // Adjust as needed

                                                if (-offsetX.value > swipeThreshold) {
                                                    // Animate the item off the screen to the left
                                                    offsetX.animateTo(
                                                        targetValue = -1000f,
                                                        animationSpec = tween(durationMillis = 300)
                                                    )
                                                    isSwiped = true
                                                    currencyViewModel.removeItem(index)

                                                    // Show Snackbar with Undo option
                                                    val snackbarResult =
                                                        snackBarHostState.showSnackbar(
                                                            message = "Item deleted",
                                                            actionLabel = "Undo",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    if (snackbarResult == SnackbarResult.ActionPerformed) {
                                                        currencyViewModel.undoRemoveItem(index)
                                                    }
                                                } else {
                                                    // Animate the item back to its original position
                                                    offsetX.animateTo(
                                                        targetValue = 0f,
                                                        animationSpec = tween(durationMillis = 300)
                                                    )
                                                }
                                            }
                                        },
                                        onHorizontalDrag = { change, dragAmount ->
                                            if (dragAmount < 0) { // Only allow left swipe
                                                change.consumeAllChanges()
                                                scope.launch {
                                                    val newOffset =
                                                        (offsetX.value + dragAmount).coerceAtMost(0f)
                                                    offsetX.snapTo(newOffset)
                                                }
                                            }
                                        }
                                    )
                                }
                                // Apply the animated offset to the item
                                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                        ) {
                            // Background delete icon with fading effect based on swipe progress
                            val deleteIconAlpha = (-offsetX.value / 150f).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(end = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red.copy(alpha = deleteIconAlpha),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Foreground item content
                            CurrencySelectorItem(
                                currencyOptions = CurrencyOptionsData.options,
                                value = converterUIState.valueTexts[index],
                                onAmountChange = { newAmount ->
                                    currencyViewModel.onAmountChange(newAmount, index)
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
                } else {

                    CurrencySelectorItem(
                        currencyOptions = CurrencyOptionsData.options,
                        value = converterUIState.valueTexts[index],
                        onAmountChange = { newAmount ->
                            currencyViewModel.onAmountChange(newAmount, index)
                        },
                        onCurrencyChange = { newCurrency ->
                            currencyViewModel.onCurrencyChange(newCurrency, index)
                        },
                        selectedCurrency = converterUIState.currencies[index],
                        label = R.string.base_currency_input,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(vertical = 4.dp)
                    )
                }
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
            .wrapContentHeight()
            .then(modifier)
    ) {
        Box(
            modifier = Modifier
                .weight(3f)
                .align(Alignment.CenterVertically)
        ) {
            EditNumberField(
                label = selectedCurrency,
                keyboardOptions = keyboardOptions,
                value = value,
                onValueChange = onAmountChange,
            )
        }
        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .weight(1.2f)
                .padding(top = 8.dp)

        ) {
            DropdownMenuSpinner(
                optionsList = currencyOptions,
                selectedCurrency = selectedCurrency,
            ) { currency ->
                onCurrencyChange(currency)
            }
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

        LaunchedEffect(selectedCurrency) {
            selectedOptionText = selectedCurrency
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
                    focusedContainerColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.onTertiary,
                    focusedTextColor = MaterialTheme.colorScheme.outlineVariant,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTrailingIconColor = MaterialTheme.colorScheme.tertiary,
                    focusedTrailingIconColor = MaterialTheme.colorScheme.onTertiary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.tertiary,
                    focusedIndicatorColor = MaterialTheme.colorScheme.onTertiary

                ),
                shape = RoundedCornerShape(15.dp),
                singleLine = true,
                modifier = Modifier.menuAnchor(),
                readOnly = true
            )

            if (expanded) {
                DropdownMenu(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.onTertiary)
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

        var internalValue by remember { mutableStateOf(value) }


        LaunchedEffect(value) {
            internalValue = value
        }


        OutlinedTextField(
            value = internalValue,
            onValueChange = { newValue ->
                internalValue = newValue
                onValueChange(newValue)
            },
            label = { Text(label) },
            shape = RoundedCornerShape(15.dp),
            colors = OutlinedTextFieldDefaults.colors(

                focusedTextColor = MaterialTheme.colorScheme.tertiary,
                unfocusedTextColor = MaterialTheme.colorScheme.tertiary,

                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onTertiary,

                focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                unfocusedLabelColor = MaterialTheme.colorScheme.tertiary,

            ),
            modifier = modifier,
            singleLine = true,
            keyboardOptions = keyboardOptions
        )
    }

@Composable
fun AddMoreContainersBtn(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = { onClick() },
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier
            .padding(16.dp)
            .size(75.dp)
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




