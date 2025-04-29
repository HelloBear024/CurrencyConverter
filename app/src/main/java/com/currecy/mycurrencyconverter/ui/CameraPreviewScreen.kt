package com.currecy.mycurrencyconverter.ui

import android.content.ContentUris
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.currecy.mycurrencyconverter.R
import com.currecy.mycurrencyconverter.data.CurrencyOptionsData
import com.currecy.mycurrencyconverter.model.cameraModel.CameraViewModel
import com.currecy.mycurrencyconverter.utills.ui_utills.DropdownMenuSpinner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewScreen(
    cameraViewModel: CameraViewModel = hiltViewModel(),
    hazeState: HazeState,
    onImageSelected: (Uri) -> Unit
) {

    val converterUIStateCamera by cameraViewModel.converterUIState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var previousDetectedText by remember { mutableStateOf("") }
    var detectionStartTime by remember { mutableStateOf(0L) }
    var conversionTriggered by remember { mutableStateOf(false) }


    // State for detected text and its position
    var detectedText by remember { mutableStateOf("") }
    var textPosition by remember { mutableStateOf<android.graphics.Rect?>(null) }
    var layoutSize by remember { mutableStateOf(IntSize(0, 0)) }

    // Define the capture area (in dp) where text detection will occur
    val captureAreaWidth = 300.dp
    val captureAreaHeight = 200.dp
    val captureAreaOffsetY = 150.dp
    var clipBorderColor by remember { mutableStateOf(Color.Gray) }
    var rectangleBounds by remember { mutableStateOf(RectF()) }
    // Get the current screen density in a composable context
    val density = LocalDensity.current


    var firstImageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { onImageSelected(it) }
        }
    )

    val context = LocalContext.current

    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted){
            cameraPermissionState.launchPermissionRequest()
        }
    }


    val permissionState = rememberPermissionState(
        permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
    )

    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) {
            firstImageUri = getFirstImageFromGallery(context)
        } else if (permissionState.status.shouldShowRationale) {

        } else {

        }
    }

    LaunchedEffect(Unit) {

        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        } else {

            firstImageUri = getFirstImageFromGallery(context)
        }
    }



    LaunchedEffect(Unit) {
        firstImageUri = getFirstImageFromGallery(context)
        Log.d("FirstImageUri", "First Image URI: $firstImageUri")
    }


    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
            .hazeSource(hazeState)
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


        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp)
            .zIndex(1f)
        ) {
            if (converterUIStateCamera.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                DropdownMenuItemRow(
                    currencyOptions = CurrencyOptionsData.options,
                    selectedCurrencyFrom = converterUIStateCamera.selectedCurrencyFrom,
                    selectedCurrencyTo = converterUIStateCamera.selectedCurrencyTo,
                    onCurrencyFromChange = { newCurrency ->
                        cameraViewModel.onCurrencyFromChange(newCurrency)
                    },
                    onCurrencyToChange = { newCurrency ->
                        cameraViewModel.onCurrencyToChange(newCurrency)
                    },
                    onSwitchCurrencies = {
                        cameraViewModel.switchCurrencies()
                    },
                    modifier = Modifier.zIndex(3f)
                )
            }
        }


        Box(
            Modifier.fillMaxSize()
        ) {

            TransparentClipLayout(
                modifier = Modifier.fillMaxSize().zIndex(1f),
                width = captureAreaWidth,
                height = captureAreaHeight,
                offsetY = captureAreaOffsetY,
                color = clipBorderColor
            )

            if (converterUIStateCamera.detectedNumber != null && converterUIStateCamera.conversionResult.isNotEmpty())  {
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
                        text = "${converterUIStateCamera.selectedCurrencyTo.uppercase()}: ${converterUIStateCamera.conversionResult}",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
                }
            }

        }

        Box(
            modifier = Modifier
                .wrapContentSize()
                .padding(
                    bottom = 140.dp,
                    start = 30.dp
                )
                .background(
                    Color.Gray,
                    shape = RoundedCornerShape(15.dp)
                )
                .align(Alignment.BottomStart)
                .clickable {
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
                        modifier = Modifier
                            .size(85.dp)
                            .clip(RoundedCornerShape(15.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

    }


    if (detectedText != previousDetectedText) {
        conversionTriggered = false
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


@Composable
fun DropdownMenuItemRow(
    currencyOptions: List<Pair<String, String>>,
    selectedCurrencyFrom: String,
    selectedCurrencyTo: String,
    onCurrencyFromChange: (String) -> Unit,
    onCurrencyToChange: (String) -> Unit,
    onSwitchCurrencies: () -> Unit,
    modifier: Modifier = Modifier) {

    var isSwitched by remember { mutableStateOf(false) }

    val dropdownSize by animateDpAsState(targetValue = if (isSwitched) 70.dp else 56.dp)


    Row(
        modifier = modifier.padding(horizontal = 32.dp)
    ) {

        Box(modifier =  Modifier.weight(1f)
            .padding(
                start = 10.dp,
                end = 10.dp
            )
            .height(dropdownSize)) {

            DropdownMenuSpinner(
                optionsList = currencyOptions,
                selectedCurrency = selectedCurrencyFrom,
                onCurrencySelected = onCurrencyFromChange,
                backgroundColor = Color(0xFFFD5B66),
                textColor = Color.White,
                trailingColor = Color.White,
                borderColor = Color.Transparent,
            )
        }

        IconButton(
            onClick = {
                onSwitchCurrencies()
                isSwitched = !isSwitched
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.switch_sides_button),
                contentDescription = "Switch Currency Place",
                tint = Color(0xFFFD5B66)
            )
        }

        Box(modifier = Modifier
            .weight(1f)
            .padding(
                start = 10.dp,
                end = 10.dp
            )
            .height(dropdownSize)
        ) {
            DropdownMenuSpinner(
                optionsList = currencyOptions,
                selectedCurrency = selectedCurrencyTo,
                onCurrencySelected = onCurrencyToChange,
                backgroundColor = Color(0xFFFD5B66),
                textColor = Color.White,
                trailingColor = Color.White,
                borderColor = Color.Transparent,
            )
        }
    }

}
