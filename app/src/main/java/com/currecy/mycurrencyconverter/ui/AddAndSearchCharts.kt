package com.currecy.mycurrencyconverter.ui

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.currecy.mycurrencyconverter.data.CurrencyOptionsData
import com.currecy.mycurrencyconverter.model.CardCurrencyViewModel
import com.currecy.mycurrencyconverter.model.ChartCurrencyState
import com.currecy.mycurrencyconverter.ui.theme.MyCurrencyConverterTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@Composable
fun AddAndSearchChartsApp(
    navController: NavController,
){
    val detailViewModel: CardCurrencyViewModel = hiltViewModel()
    var showDialog by remember { mutableStateOf(false) }

    val conversions by detailViewModel.filteredConversions.collectAsState()

    // **Collect the Current Search Query from the ViewModel**
    val searchQuery by detailViewModel.searchQuery.collectAsState()

//    val filteredConversions = if (searchQuery.isEmpty()) {
//        allConversions
//    } else {
//        allConversions.filter {
//            it.sourceCurrency.contains(searchQuery, ignoreCase = true) ||
//                    it.targetCurrency.contains(searchQuery, ignoreCase = true)
//        }
//    }


    LaunchedEffect(conversions) {
        Log.d("AddAndSearchChartsApp", "Current conversions: $conversions")
    }

        Scaffold(
            topBar = { SearchingBar(
                query = searchQuery,
                onQueryChange = { newQuery -> detailViewModel.setSearchQuery(newQuery)}
            ) } ,
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
                .padding(innerPadding))
            {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 10.dp)
                )  {
                    ConversionList(
                        conversions = conversions,
                        onItemClick = { conversion ->
                            navController.navigate("detail/${conversion.id}")
                        },
                        onDelete = { conversion ->
                            detailViewModel.deleteConversion(conversion)
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
                            detailViewModel.addConversion(source, target)
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
fun SearchingBar(
    query: String,
    onQueryChange: (String) -> Unit
){
    var active by rememberSaveable { mutableStateOf(false) }

    // Focus Manager to handle focus state
    val focusManager = LocalFocusManager.current

    SearchBar(
        query = query,
        onQueryChange = { newQuery ->
            Log.d("SearchingBar", "onQueryChange: $newQuery")
            onQueryChange(newQuery)
        },
        onSearch = {
            Log.d("SearchingBar", "onSearch triggered with query: $query")
            // Optionally handle the search action here
            // For example, you could trigger a specific search event in the ViewModel
            // Currently, it just closes the keyboard and the suggestions
            active = false
            focusManager.clearFocus()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        placeholder = { Text("Search Currency") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = {
                    Log.d("SearchingBar", "Clear Search clicked")
                    onQueryChange("")
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear Search")
                }
            }
        },
        active = active,
        onActiveChange = { isActive ->
            Log.d("SearchingBar", "onActiveChange: $isActive")
            active = isActive
        }
    ) {
        // Suggestions list directly within the SearchBar's content slot
        if (active && query.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                val suggestions = listOf("USD", "EUR", "JPY", "GBP").filter {
                    it.contains(query, ignoreCase = true)
                }
                items(suggestions) { suggestion ->
                    Text(
                        text = suggestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Log.d("SearchingBar", "Suggestion clicked: $suggestion")
                                onQueryChange(suggestion)
                                active = false
                                focusManager.clearFocus()
                            }
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SearchingBar(
//    query: String,
//    onQueryChange: (String) -> Unit
//){
//    var active by rememberSaveable { mutableStateOf(false) }
//
//    // Focus Manager to handle focus state
//    val focusManager = LocalFocusManager.current
//
//    SearchBar(
//        query = query,
//        onQueryChange = { newQuery ->
//            Log.d("SearchingBar", "onQueryChange: $newQuery")
//            onQueryChange(newQuery)
//        },
//        onSearch = {
//            Log.d("SearchingBar", "onSearch triggered with query: $query")
//            // Optionally handle the search action
//            active = false
//            focusManager.clearFocus()
//        },
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp),
//        placeholder = { Text("Search Currency") },
//        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
//        trailingIcon = {
//            if (query.isNotEmpty()) {
//                IconButton(onClick = {
//                    Log.d("SearchingBar", "Clear Search clicked")
//                    onQueryChange("")
//                }) {
//                    Icon(Icons.Default.Close, contentDescription = "Clear Search")
//                }
//            }
//        },
//        active = active,
//        onActiveChange = { isActive ->
//            Log.d("SearchingBar", "onActiveChange: $isActive")
//            active = isActive
//        }
//    ) {
//        // Suggestions list directly within the SearchBar's content slot
//        if (active && query.isNotEmpty()) {
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(MaterialTheme.colorScheme.surface)
//            ) {
//                val suggestions = listOf("USD", "EUR", "JPY", "GBP").filter {
//                    it.contains(query, ignoreCase = true)
//                }
//                items(suggestions) { suggestion ->
//                    Text(
//                        text = suggestion,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable {
//                                Log.d("SearchingBar", "Suggestion clicked: $suggestion")
//                                onQueryChange(suggestion)
//                                active = false
//                                focusManager.clearFocus()
//                            }
//                            .padding(16.dp),
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//

//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SearchingBar(
//    query: String,
//    onQueryChange: (String) -> Unit
//){
//    var expanded by rememberSaveable { mutableStateOf(false) }
//
//    // **Focus Manager to handle focus state**
//    val focusManager = LocalFocusManager.current
//
//    SearchBar(
//        query = query,
//        onQueryChange = { newQuery ->
//            onQueryChange(newQuery)
//            expanded = newQuery.isNotEmpty()
//        },
//        onSearch = {
//            // Optionally handle the search action
//            expanded = false
//            focusManager.clearFocus()
//        },
//        modifier = Modifier.fillMaxWidth(),
//        placeholder = { Text("Search Currency") },
//        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
//        trailingIcon = {
//            if (query.isNotEmpty()) {
//                IconButton(onClick = { onQueryChange("") }) {
//                    Icon(Icons.Default.Close, contentDescription = "Clear Search")
//                }
//            }
//        },
//        active = expanded,
//        onActiveChange = { isActive ->
//            expanded = isActive
//        }
//    ) {
//        // **Suggestions Dropdown**
//        DropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            // Example suggestions; replace with actual logic or ViewModel data
//            val suggestions = listOf("USD", "EUR", "JPY", "GBP").filter {
//                it.contains(query, ignoreCase = true)
//            }
//            suggestions.forEach { suggestion ->
//                DropdownMenuItem(
//                    text = { Text(suggestion) },
//                    onClick = {
//                        onQueryChange(suggestion)
//                        expanded = false
//                        focusManager.clearFocus()
//                    }
//                )
//            }
//        }
//    }
//}




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

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(conversions, key = { it.id }) { conversion ->
            var isSwiped by remember { mutableStateOf(false) }

            if (!isSwiped) {
                // Create an Animatable for the horizontal offset
                val offsetX = remember { Animatable(0f) }
                val scope = rememberCoroutineScope()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(vertical = 4.dp)
                        .background(Color.Transparent)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    scope.launch {
                                        // Define the swipe threshold (e.g., 30% of the screen width)
                                        val swipeThreshold = 300f

                                        if (-offsetX.value > swipeThreshold) {
                                            // Animate the item off the screen to the left
                                            offsetX.animateTo(
                                                targetValue = -1000f,
                                                animationSpec = tween(durationMillis = 300)
                                            )
                                            isSwiped = true
                                            onDelete(conversion)
                                        } else {
                                            // Animate the item back to its original position
                                            offsetX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = tween(durationMillis = 300)
                                            )
                                        }
                                    }
                                },
                                onHorizontalDrag  = { change, dragAmount ->
                                    change.consume()
                                    // Update the offset, limiting to left swipe only
                                    val newOffset = (offsetX.value + dragAmount).coerceAtMost(0f)
                                    scope.launch {
                                        offsetX.snapTo(newOffset)
                                    }
                                }
                            )
                        }
                        // Apply the animated offset to the item
                        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                ) {
                    // Background delete icon with fading effect based on swipe progress
                    val deleteIconAlpha = (-offsetX.value / 300f).coerceIn(0f, 1f)
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
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }

                    // Foreground item content
                    ConversionCard(
                        conversion = conversion,
                        onItemClick = { onItemClick(conversion) },
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

//
//@Composable
//fun ConversionList(
//    conversions: List<ChartCurrencyState>,
//    onItemClick: (ChartCurrencyState) -> Unit,
//    onDelete: (ChartCurrencyState) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val listState = rememberLazyListState()
//
//    LazyColumn(
//        state = listState,
//        modifier = modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        items(conversions, key = { it.id }) { conversion ->
//            var isSwiped by remember { mutableStateOf(false) }
//
//            if (!isSwiped) {
//                var offsetX by remember { mutableStateOf(0f) }
//                val scope = rememberCoroutineScope()
//
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .wrapContentHeight()
//                        .padding(vertical = 4.dp)
//                        .background(Color.Transparent)
//                        .pointerInput(Unit) {
//                            detectHorizontalDragGestures { change, dragAmount ->
//                                change.consume()
//                                offsetX += dragAmount
//                                // Limit swipe to left only
//                                if (offsetX > 0) {
//                                    offsetX = 0f
//                                }
//                                // Threshold for swipe to delete
//                                if (-offsetX > 300f) {
//                                    isSwiped = true
//                                    onDelete(conversion)
//                                }
//                            }
//                        }
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(end = 16.dp),
//                        contentAlignment = Alignment.CenterEnd
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Delete,
//                            contentDescription = "Delete",
//                            tint = Color.White
//                        )
//                    }
//
//                    ConversionCard(
//                        conversion = conversion,
//                        onItemClick = { onItemClick(conversion)},
//                    )
//                }
//            }
//        }
//    }
//}

@Composable
fun ConversionCard(
    conversion: ChartCurrencyState,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentRate = conversion.currentRate
    val percentageChange = conversion.percentageChange
    Log.d("AddAndSearchChart", "${percentageChange}")


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentHeight()
                        .align(Alignment.CenterVertically)
                ) {

                    Text(
                        text = "${conversion.sourceCurrency.uppercase()}/${conversion.targetCurrency.uppercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 25.sp
                    )
                }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = String.format("%.5f",currentRate),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp
                )

                Spacer(Modifier.height(5.dp))

                Box(
                    modifier = Modifier
                        .background(
                            color = if (percentageChange >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.errorContainer,  // Green for positive, Red for negative
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${if (percentageChange >= 0) "+" else ""}${
                            String.format(
                                "%.2f",
                                percentageChange
                            )
                        }%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
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

    }
}