package com.currecy.mycurrencyconverter.ui

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.currecy.mycurrencyconverter.R
import com.currecy.mycurrencyconverter.data.CurrencyOptionsData
import com.currecy.mycurrencyconverter.model.searchChart.CardCurrencyViewModel
import com.currecy.mycurrencyconverter.model.searchChart.ChartCurrencyState
import com.currecy.mycurrencyconverter.ui.theme.MyCurrencyConverterTheme
import com.currecy.mycurrencyconverter.utills.ui_utills.CurrencyConversionDialog
import com.currecy.mycurrencyconverter.utills.ui_utills.CustomButton
import com.currecy.mycurrencyconverter.utills.ui_utills.GlassmorphicContainerSearchbar
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@Composable
fun AddAndSearchChartsApp(
    navController: NavController,
    hazeState: HazeState
) {
    val detailViewModel: CardCurrencyViewModel = hiltViewModel()
    var showDialog by remember { mutableStateOf(false) }

    val conversions by detailViewModel.filteredConversions.collectAsState()

    val searchQuery by detailViewModel.searchQuery.collectAsState()

    LaunchedEffect(conversions) {
        Log.d("AddAndSearchChartsApp", "Current conversions: $conversions")
    }

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)

    val statusBarDp = with(density) { statusBarHeight.toDp() }

    var isSearchBarActive by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->

        AsyncImage(
            model = R.drawable.background_new,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(3f)
        ) {
            val maxWith = this.maxWidth
            val maxHeight = this.maxHeight

            Box(modifier = Modifier.zIndex(10f)
                .padding(   top = 40.dp,
                            start = if (isSearchBarActive) 0.dp else 16.dp,
                    end = if (isSearchBarActive) 0.dp else 16.dp
                            )
            ) {
                    SearchingBar(
                        query = searchQuery,
                        hazeState = hazeState,
                        onQueryChange = { newQuery -> detailViewModel.setSearchQuery(newQuery) },
                        isSearchBarActive = isSearchBarActive,
                        onDismiss = { newValue -> isSearchBarActive = newValue }
                    )

            }


            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                    .align(Alignment.BottomCenter)
                    .height(maxHeight / 1.15f)
                    .width(maxWith)
                    .background( Color(0x99FFFFFF) )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 10.dp)
                    ) {
                        ConversionList(
                            conversions = conversions,
                            onItemClick = { conversion ->
                                navController.navigate("chart_page?id=${conversion.id}")
                            },
                            onDelete = { conversion ->
                                detailViewModel.deleteConversion(conversion)
                            },
                            modifier = Modifier.weight(1f),
                            onAddButtonClick = { showDialog = true },
                            hazeState = hazeState
                        )
                    }
                    if (showDialog) {
                        CurrencyConversionDialog(
                            onDismissRequest = { showDialog = false },
                            onConfirm = { source, target ->
                                showDialog = false
                                Log.d(
                                    "AddAndSearchChartsApp",
                                    "Confirming conversion: $source -> $target"
                                )
                                detailViewModel.addConversion(source, target)
                            },

                            optionsList = CurrencyOptionsData.options,
                            initialSourceCurrency = "usd",
                            initialTargetCurrency = "eur",
                            hazeState = hazeState
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchingBar(
    query: String,
    hazeState: HazeState,
    onQueryChange: (String) -> Unit,
    isSearchBarActive: Boolean,
    onDismiss: (Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .fillMaxWidth()
            .let {
                if (!isSearchBarActive) {
                    it.hazeEffect(
                        state = hazeState,
                        style = HazeMaterials.ultraThin()
                    ) { blurRadius = 20.dp }
                } else it
            }
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { newQuery ->
                onQueryChange(newQuery)
                expanded = newQuery.isNotEmpty()
            },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 58.dp)
                .clip(RoundedCornerShape(30.dp)),
            placeholder = {
                Text(
                    "Search Currency",
                    color = Color(0xFFFD5B66),
                    fontSize = 16.sp
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        onQueryChange("")
                        expanded = false
                    }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear Search",
                            tint = Color(0xFFFD5B66)
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = Color(0xFFFD5B66)
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,

                focusedTextColor = Color.White,


                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent

            ),
            singleLine = true
        )

        DropdownMenu(
            expanded = expanded && isSearchBarActive,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
        ) {
            val suggestions = CurrencyOptionsData.options.filter { (name, code) ->
                name.contains(query, ignoreCase = true) ||
                        code.contains(query, ignoreCase = true)
            }

            suggestions.forEach { (name, code) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "$name $code",
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFD5B66),
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onQueryChange("$name $code")
                        expanded = false
                        focusManager.clearFocus()
                    }
                )
            }
        }
    }
}

@Composable
fun ConversionList(
    conversions: List<ChartCurrencyState>,
    onItemClick: (ChartCurrencyState) -> Unit,
    onDelete: (ChartCurrencyState) -> Unit,
    onAddButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState
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
                                        val swipeThreshold = 300f

                                        if (-offsetX.value > swipeThreshold) {
                                            offsetX.animateTo(
                                                targetValue = -1000f,
                                                animationSpec = tween(durationMillis = 300)
                                            )
                                            isSwiped = true
                                            onDelete(conversion)
                                        } else {
                                            offsetX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = tween(durationMillis = 300)
                                            )
                                        }
                                    }
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    val newOffset = (offsetX.value + dragAmount).coerceAtMost(0f)
                                    scope.launch {
                                        offsetX.snapTo(newOffset)
                                    }
                                }
                            )
                        }
                        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                ) {
                    ConversionCard(
                        conversion = conversion,
                        onItemClick = { onItemClick(conversion) },
                        modifier = Modifier
                            .fillMaxSize(),
                        hazeState = hazeState
                    )
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                CustomButton(
                    onClick = onAddButtonClick
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

@Composable
fun ConversionCard(
    conversion: ChartCurrencyState,
    onItemClick: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val currentRate = conversion.currentRate
    val percentageChange = conversion.percentageChange
    Log.d("AddAndSearchChart", "${percentageChange}")

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .fillMaxWidth()
            .clickable { onItemClick() }
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.ultraThin()
            ) {
                blurRadius = 50.dp
            },
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 10.dp),
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
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = String.format("%.5f",currentRate),
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(5.dp))

                Box(
                    modifier = Modifier
                        .background(
                            color = if (percentageChange >= 0) Color(0x80077D07) else Color( 0x99FD5B66 ),
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