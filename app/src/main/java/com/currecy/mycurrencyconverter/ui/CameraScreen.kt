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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.currecy.mycurrencyconverter.R
import com.currecy.mycurrencyconverter.data.CurrencyOptionsData
import com.currecy.mycurrencyconverter.database.CurrencyRateDao
import com.currecy.mycurrencyconverter.model.CameraViewModel
import com.currecy.mycurrencyconverter.model.CurrencyViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
    fun CameraConversionScreen(currencyDao: CurrencyRateDao) {
    val cameraViewModel: CameraViewModel = viewModel(
        factory = CurrencyViewModelFactory(currencyDao)
    )
    val converterUIStateCamera by cameraViewModel.converterUIState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var previousDetectedText by remember { mutableStateOf("") }
    var detectionStartTime by remember { mutableStateOf(0L) }
    var conversionTriggered by remember { mutableStateOf(false) }


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
    var rectangleBounds by remember { mutableStateOf(RectF()) }
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


//    LaunchedEffect(layoutSize) {
//        if (layoutSize.width > 0 && layoutSize.height > 0) {
//            val captureAreaLeftPx = ((layoutSize.width - with(density) { captureAreaWidth.toPx() }) / 2f)
//            val captureAreaTopPx = with(density) { captureAreaOffsetY.toPx() }
//            val captureAreaRightPx = captureAreaLeftPx + with(density) { captureAreaWidth.toPx() }
//            val captureAreaBottomPx = captureAreaTopPx + with(density) { captureAreaHeight.toPx() }
//
//            rectangleBounds = RectF(
//                captureAreaLeftPx,
//                captureAreaTopPx,
//                captureAreaRightPx,
//                captureAreaBottomPx
//            )
//
//            Log.d("RectangleBounds", "rectangleBounds: $rectangleBounds")
//        } else {
//            Log.d("RectangleBounds", "layoutSize not ready yet")
//        }
//    }


    DropdownMenuItemRow(
        currencyOptions = CurrencyOptionsData.options,
        onCurrencyChange = { newAmount ->
            cameraViewModel.onAmountChange()
        },
        selectedCurrency = converterUIStateCamera.selectedCurrencyTo
    )


    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val boxWidth = constraints.maxWidth.toFloat()
        val boxHeight = constraints.maxHeight.toFloat()

        Log.d("BoxWithConstraints", "boxWidth: $boxWidth, boxHeight: $boxHeight")

        // Calculate rectangle bounds based on BoxWithConstraints
        with(density) {
            val rectLeft = (boxWidth - captureAreaWidth.toPx()) / 2f
            val rectTop = captureAreaOffsetY.toPx()
            val rectRight = rectLeft + captureAreaWidth.toPx()
            val rectBottom = rectTop + captureAreaHeight.toPx()

            rectangleBounds = RectF(
                rectLeft,
                rectTop,
                rectRight,
                rectBottom
            )

            Log.d("RectangleBounds", "rectangleBounds: $rectangleBounds")
        }

        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            rectangleBounds = rectangleBounds, // Initial default bounds
            onTextDetected = { detectedTextValue, boundingBox ->
                Log.d("Camera Screen", "Text found + $detectedTextValue ")
                val currentTime = System.currentTimeMillis()
                if (detectedTextValue == previousDetectedText) {
                    if (!conversionTriggered && currentTime - detectionStartTime >= 2000) {
                        // Detected text has been the same for more than 2 seconds
                        conversionTriggered = true
                        // Perform conversion
                        coroutineScope.launch {
                            cameraViewModel.onNumberDetected(detectedTextValue)
                        }
                    }
                } else {
                    // Detected text has changed
                    previousDetectedText = detectedTextValue
                    detectionStartTime = currentTime
                    conversionTriggered = false
                }
                detectedText = detectedTextValue
                textPosition = boundingBox
                clipBorderColor = Color(0xFFFFD700)
            }
        )


        Box(
            Modifier.fillMaxSize()
        ) {

            TransparentClipLayout(
                modifier = Modifier.fillMaxSize(),
                width = captureAreaWidth,
                height = captureAreaHeight,
                offsetY = captureAreaOffsetY,
                color = clipBorderColor
            )



            if (converterUIStateCamera.detectedNumber != null) {
                Log.d("Camera Screen", "if statment is triggered but no fucking text display ")
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 100.dp)

                ) { Box(
                    modifier = Modifier
                        .background(Color.Gray.copy(alpha = 0.7f))
                        .clip(RoundedCornerShape(40.dp))

                ) {
                    Text(
                        text = "Detected Number: ${converterUIStateCamera.detectedNumber}",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
                }
            }

        }








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
            }
        }


//         Display the conversion rate in a grey box when conversion happens
//        if (converterUIStateCamera.detectedNumber != null) {
//            Box(
//                modifier = Modifier
//                    .align(Alignment.Center)
//                    .background(Color.Gray.copy(alpha = 0.7f))
//                    .padding(16.dp)
//
//            ) {
//                Text(
//                    text = "Detected Number: ${converterUIStateCamera.detectedNumber}",
//                    color = Color.White,
//                    style = MaterialTheme.typography.headlineLarge
//                )
//            }
//            }
//
        }


        if (detectedText != previousDetectedText) {
            conversionTriggered = false
            // Reset other states if necessary
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
            optionsList = currencyOptions,
            selectedCurrency = selectedCurrency,
            modifier = Modifier.weight(1f)
                .padding(start = 10.dp,
                         end = 10.dp),
        ) {
                currency ->
            onCurrencyChange(currency)

        }

        IconButton(
            onClick = {},
        ) {
            Icon(painter = painterResource(R.drawable.switch_sides_button),
                contentDescription = "Switch Currency Place")
        }


        DropdownMenuSpinner(
            optionsList = currencyOptions,
            selectedCurrency = selectedCurrency,
            modifier = Modifier.weight(1f)
                .padding(start = 10.dp,
                    end = 10.dp),
        ) {
            currency ->
            onCurrencyChange(currency)
        }
    }
    
}

