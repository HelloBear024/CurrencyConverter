package com.currecy.mycurrencyconverter.ui

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.glance.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.currecy.mycurrencyconverter.R
import com.currecy.mycurrencyconverter.data.CurrencyOptionsData
import com.currecy.mycurrencyconverter.model.homeModel.CurrencyViewModel
import com.currecy.mycurrencyconverter.ui.theme.MyCurrencyConverterTheme
import com.currecy.mycurrencyconverter.utills.ui_utills.CustomButton
import com.currecy.mycurrencyconverter.utills.ui_utills.DropdownMenuSpinner
import com.currecy.mycurrencyconverter.utills.ui_utills.EditNumberField
import com.currecy.mycurrencyconverter.utills.ui_utills.GlassmorphicContainerTextInputs
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


fun getOrdinal(day: Int): String {
    return when {
        day in 11..13 -> "$day" + "th"
        day % 10 == 1 -> "$day" + "st"
        day % 10 == 2 -> "$day" + "nd"
        day % 10 == 3 -> "$day" + "rd"
        else -> "$day" + "th"
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun MainScreenCurrencyConverterEditTextView(
    hazeState: HazeState,
    currencyViewModel: CurrencyViewModel = hiltViewModel()
) {
    val converterUIState by currencyViewModel.currencyRatesState.collectAsState()
    val listState = rememberLazyListState()
    val snackBarHostState = remember { SnackbarHostState() }

    val thisHazeStateComp = remember { HazeState() }

    val currentDate = LocalDate.now()
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE")
    val day = currentDate.format(dayOfWeekFormatter)
    val dayOfMonthOrdinal = getOrdinal(currentDate.dayOfMonth)

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)

    val statusBarDp = with(density) { statusBarHeight.toDp() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        modifier = Modifier.fillMaxSize()
    ) {  _ ->

        AsyncImage(
            model = R.drawable.background_new,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
        )



        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
                .zIndex(3f)
        ) {
            val maxWith = this.maxWidth
            val maxHeight = this.maxHeight

            Box() {
                Column(
                    modifier = Modifier
                        .padding(
                            top = statusBarDp + 8.dp,
                            start = 24.dp,
                            end = 24.dp,
                        )
                ) {
                    Text(
                        text = "Welcome",
                        fontSize = 30.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = "$day $dayOfMonthOrdinal",
                        fontSize = 18.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                    .align(Alignment.BottomCenter)
                    .height(maxHeight / 1.15f)
                    .width(maxWith)
                    .background(Color(0x99FFFFFF))
            ) {

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding()
                        .padding(16.dp)
                        .padding(bottom = 0.dp)
                ) {

                    items(
                        count = converterUIState.numberOfItems,
                        key = { index -> index }
                    ) { index ->

                        val canSwipe = converterUIState.numberOfItems > 2

                        if (canSwipe) {
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
                                                        val swipeThreshold = 150f

                                                        if (-offsetX.value > swipeThreshold) {

                                                            offsetX.animateTo(
                                                                targetValue = -1000f,
                                                                animationSpec = tween(durationMillis = 300)
                                                            )
                                                            isSwiped = true
                                                            currencyViewModel.removeItem(index)
                                                            val snackbarResult =
                                                                snackBarHostState.showSnackbar(
                                                                    message = "Item deleted",
                                                                    actionLabel = "Undo",
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            if (snackbarResult == SnackbarResult.ActionPerformed) {
                                                                currencyViewModel.undoRemoveItem(
                                                                    index
                                                                )
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
                                                                (offsetX.value + dragAmount).coerceAtMost(
                                                                    0f
                                                                )
                                                            offsetX.snapTo(newOffset)
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                                ) {
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
                                        hazeState = hazeState,
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
                                hazeState = hazeState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }

                    item {
                        if (converterUIState.numberOfItems < 6) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                CustomButton(
                                    onClick = {
                                        currencyViewModel.addMoreItems()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add",
                                        tint = Color(0xFFFD5B66)
                                    )
                                }
                            }
                        }
                    }
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
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {

    Row(modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(0.7f)
        ) {
            GlassmorphicContainerTextInputs(
                hazeState = hazeState
            ) {
                EditNumberField(
                    label = selectedCurrency,
                    keyboardOptions = keyboardOptions,
                    value = value,
                    onValueChange = onAmountChange,
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .weight(0.3f)
        ) {
            GlassmorphicContainerTextInputs(
                hazeState = hazeState
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




