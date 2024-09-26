package com.currecy.mycurrencyconverter.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.currecy.mycurrencyconverter.data.CurrencyOptionsData
import com.currecy.mycurrencyconverter.model.CardCurrencyViewModel
import com.currecy.mycurrencyconverter.model.ChartCurrencyState
import com.currecy.mycurrencyconverter.ui.theme.MyCurrencyConverterTheme


@Composable
fun AddAndSearchChartsApp(
    cardCurrencyViewModel: CardCurrencyViewModel,
    navController: NavController
){

    var showDialog by remember { mutableStateOf(false) }
    val conversions by cardCurrencyViewModel.conversions.collectAsState()


    LaunchedEffect(conversions) {
        Log.d("AddAndSearchChartsApp", "Current conversions: $conversions")
    }

        Scaffold(
            floatingActionButton = {
                AddMoreContainersBtn(onClick = {
                    showDialog = true
                })
            },
            modifier = Modifier
                .fillMaxSize()
        ) { innerPadding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                )  {
                    SearchingBar()
                    ConversionList(
                        conversions = conversions,
                        onItemClick = { conversion ->
                            navController.navigate("detail/${conversion.id}")
                        },
                        onDelete = { conversion ->
                            cardCurrencyViewModel.deleteConversion(conversion)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (showDialog) {
                    CurrencyConversionDialog(
                        onDismissRequest = { showDialog = false },
                        onConfirm = { source, target ->
                            showDialog = false
                            // Add new conversion to the list
                            Log.d("AddAndSearchChartsApp", "Confirming conversion: $source -> $target")
                            cardCurrencyViewModel.addConversion(source, target)
                        },

                        optionsList = CurrencyOptionsData.options,
                        initialSourceCurrency = "usd",
                        initialTargetCurrency = "eur"
                    )
                }
            }
        }
    }





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchingBar(){
    var text by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .semantics { isTraversalGroup = true }
    ) {
        SearchBar(
            modifier = Modifier.align(Alignment.TopCenter).semantics { traversalIndex = 0f },
            inputField = {
                SearchBarDefaults.InputField(
                    query = text,
                    onQueryChange = { text = it },
                    onSearch = { expanded = false },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = { Text("Search Currency") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                repeat(4) { idx ->
                    val resultText = "Suggestion $idx"
                    ListItem(
                        headlineContent = { Text(resultText) },
                        supportingContent = { Text("Additional info") },
                        leadingContent = { Icon(Icons.Filled.Star, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier =
                        Modifier.clickable {
                            text = resultText
                            expanded = false
                        }
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConversionDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (String, String) -> Unit,
    optionsList: List<Pair<String, String>>,
    initialSourceCurrency: String = optionsList[0].second,
    initialTargetCurrency: String = optionsList[1].second
) {
    // State for selected currencies
    var sourceCurrency by remember { mutableStateOf(initialSourceCurrency) }
    var targetCurrency by remember { mutableStateOf(initialTargetCurrency) }

    LaunchedEffect(sourceCurrency, targetCurrency) {
        Log.d("CurrencyConversionDialog", "Selected source: $sourceCurrency, target: $targetCurrency")
    }

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Source Currency Dropdown
                Text(text = "From:", style = MaterialTheme.typography.bodyMedium)
                DropdownMenuSpinner(
                    optionsList = optionsList,
                    selectedCurrency = sourceCurrency,
                    onCurrencySelected = {
                        sourceCurrency = it
                        Log.d("CurrencyConversionDialog", "Source currency selected: $it")

                    },
                    modifier = Modifier.fillMaxWidth()

                )

                Spacer(modifier = Modifier.height(16.dp))

                // Target Currency Dropdown
                Text(text = "To:", style = MaterialTheme.typography.bodyMedium)
                DropdownMenuSpinner(
                    optionsList = optionsList,
                    selectedCurrency = targetCurrency,
                    onCurrencySelected = { targetCurrency = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel")
                    }
                    TextButton(

                        onClick = {
                            Log.d("CurrencyConversionDialog", "Confirm button clicked with: $sourceCurrency -> $targetCurrency")

                            onConfirm(sourceCurrency, targetCurrency)
                        }
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}


@Composable
fun ConversionList(
    conversions: List<ChartCurrencyState>,
    onItemClick: (ChartCurrencyState) -> Unit,
    onDelete: (ChartCurrencyState) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(conversions, key = { it.id }) { conversion ->
            var offsetX by remember { mutableStateOf(0f) }
            var isSwiped by remember { mutableStateOf(false) }

            if (!isSwiped) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(vertical = 4.dp)
                        .background(Color.Transparent)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount
                                // Limit swipe to left only
                                if (offsetX > 0) {
                                    offsetX = 0f
                                }
                                // Threshold for swipe to delete
                                if (-offsetX > 300f) {
                                    isSwiped = true
                                    onDelete(conversion)
                                }
                            }
                        }
                ) {
                    // Background Red for delete
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 16.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }

                    // Foreground Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(offsetX.toInt(), 0) }
                            .alpha(if (isSwiped) 0f else 1f)
                            .clickable { onItemClick(conversion) },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${conversion.sourceCurrency.uppercase()} ➔ ${conversion.targetCurrency.uppercase()}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConversionCard(
    conversion: ChartCurrencyState,
    onItemClick: () -> Unit
) {
    Log.d("ConversionCard", "Rendering card: ${conversion.sourceCurrency} ➔ ${conversion.targetCurrency}")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${conversion.sourceCurrency} ➔ ${conversion.targetCurrency}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}




@Preview(
    showBackground = true,
    name = "Searching Component"
)
@Composable
fun ItemPreview(){
    MyCurrencyConverterTheme{
//        AddAndSearchChartsApp()
    }
}