package com.currecy.mycurrencyconverter.ui

import android.content.ContentUris
import android.graphics.RectF
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.currecy.mycurrencyconverter.database.CurrencyRateDao
import com.currecy.mycurrencyconverter.model.CameraViewModel
import com.currecy.mycurrencyconverter.model.CurrencyViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
    fun CameraConversionScreen(currencyDao: CurrencyRateDao) {
    val cameraViewModel: CameraViewModel = viewModel(
        factory = CurrencyViewModelFactory(currencyDao)
    )
    val converterUIStateCamera by cameraViewModel.converterUIState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // State for detected text and its position
    var detectedText by remember { mutableStateOf("") }
    var textPosition by remember { mutableStateOf<android.graphics.Rect?>(null) }
    var conversionRate by remember { mutableStateOf("") } // Conversion rate to be shown in a grey box
    var layoutSize by remember { mutableStateOf(IntSize(0, 0)) }
    var isFocused by remember { mutableStateOf(false) }

    // Define the capture area (in dp) where text detection will occur
    val captureAreaWidth = 300.dp
    val captureAreaHeight = 200.dp
    val captureAreaOffsetY = 150.dp
    var clipBorderColor by remember { mutableStateOf(Color.Gray) } // Initially gray
    var ignoreNewDetections by remember { mutableStateOf(false) }
    var rectangleBounds by remember { mutableStateOf<RectF?>(null) }

    // Get the current screen density in a composable context
    val density = LocalDensity.current

    val captureAreaLeftPx = with(density) { ((layoutSize.width - captureAreaWidth.toPx()) / 2) }
    val captureAreaTopPx = with(density) { captureAreaOffsetY.toPx() }
    val captureAreaRightPx = captureAreaLeftPx + with(density) { captureAreaWidth.toPx() }
    val captureAreaBottomPx = captureAreaTopPx + with(density) { captureAreaHeight.toPx() }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var firstImageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedImageUri = uri // Save the selected image URI
        }
    )

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        firstImageUri = getFirstImageFromGallery(context)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { layoutSize = it }
    ) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            rectangleBounds = RectF(), // Initial default bounds
            onTextDetected = { detectedTextValue, boundingBox ->
                coroutineScope.launch {
                    detectedText = detectedTextValue
                    textPosition = boundingBox
                    clipBorderColor = Color(0xFFFFD700) // Change the color when text is detected
                    delay(3000)
                    clipBorderColor = Color.Gray // Reset color after delay
                }
            }
        )

        TransparentClipLayout(
            modifier = Modifier.fillMaxSize(),
            width = captureAreaWidth,
            height = captureAreaHeight,
            offsetY = captureAreaOffsetY,
            color = clipBorderColor
        )


        Box(
            modifier = Modifier
                .size(115.dp)
                .padding(
                    bottom = 30.dp,
                    start = 30.dp
                )// Square box size
                .background(
                    Color.Gray,
                    shape = RoundedCornerShape(8.dp)
                ) // Background color of the button
                .align(Alignment.BottomStart) // Align to bottom-left corner
                .clickable {
                    // Trigger gallery picker when clicked
                    imagePickerLauncher.launch("image/*")
                },
            contentAlignment = Alignment.Center
        ) {

            if (firstImageUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(firstImageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "First image from gallery",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {


            }
        }



        if (detectedText.isNotEmpty() && textPosition != null) {


            val xOffset = with(density) { textPosition!!.left.toDp() }
            val yOffset = with(density) { textPosition!!.top.toDp() }
            val width = with(density) { textPosition!!.width().toDp() }
            val height = with(density) { textPosition!!.height().toDp() }


            // Display the conversion rate in a grey box when conversion happens
            if (conversionRate.isNotEmpty()) {
                Log.d("TextDetection", "Detected Rate: $conversionRate")

                val convertedYOffset = with(density) { (textPosition!!.bottom + 16).toDp() }
                Box(
                    modifier = Modifier
                        .offset(x = xOffset, y = convertedYOffset)
                        .background(Color.Gray.copy(alpha = 0.9f)) // Grey background
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Converted: $conversionRate",
                        color = Color.White
                    )
                }
            }
        }
    }
}


suspend fun getFirstImageFromGallery(context: android.content.Context): Uri? {
    return withContext(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.Images.Media._ID
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        val query = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            if (cursor.moveToFirst()) {
                val id = cursor.getLong(idColumn)
                return@withContext ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            }
        }
        return@withContext null
    }
}

// Composable that shows the selected image
@Composable
fun ShowSelectedImageScreen(imageUri: Uri) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUri)
                .crossfade(true)
                .build(),
            contentDescription = "Selected Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Scale the image to fill the container
        )
    }
}
@Composable
fun DropdownMenuItemRow(
    currencyOptions: List<Pair<String, String>>,
    onCurrencyChange: (String) -> Unit,
    selectedCurrency: String
    ,modifier: Modifier = Modifier) {
    Row {
        DropdownMenuSpinner(
            optionsList = currencyOptions
            , selectedCurrency = selectedCurrency
        ) {
                currency ->
            onCurrencyChange(currency)

        }

        DropdownMenuSpinner(
            optionsList = currencyOptions,
            selectedCurrency = selectedCurrency
        ) {
            currency ->
            onCurrencyChange(currency)
        }
    }
    
}

