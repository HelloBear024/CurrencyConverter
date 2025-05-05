package com.currecy.mycurrencyconverter.ui

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapRegionDecoder
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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


//@OptIn(ExperimentalPermissionsApi::class)
//@Composable
//fun CameraPreviewScreen(
//    cameraViewModel: CameraViewModel = hiltViewModel(),
//    hazeState: HazeState,
//    navController: NavController,
//    takePhoto: Boolean
//) {
//
//    Log.d("CameraPreviewScreen", "take photo: $takePhoto")
//
//    val converterUIStateCamera by cameraViewModel.converterUIState.collectAsState()
//    val coroutineScope = rememberCoroutineScope()
//    var previousDetectedText by remember { mutableStateOf("") }
//    var detectionStartTime by remember { mutableStateOf(0L) }
//    var conversionTriggered by remember { mutableStateOf(false) }
//
//
//
//    // State for detected text and its position
//    var detectedText by remember { mutableStateOf("") }
//    var textPosition by remember { mutableStateOf<android.graphics.Rect?>(null) }
//
//    val captureAreaWidth = 300.dp
//    val captureAreaHeight = 200.dp
//    val captureAreaOffsetY = 150.dp
//    var clipBorderColor by remember { mutableStateOf(Color.Gray) }
//    var rectangleBounds by remember { mutableStateOf(RectF()) }
//
//    val density = LocalDensity.current
//
//
//    var previewView by remember { mutableStateOf<PreviewView?>(null) }
//
//
//    var firstImageUri by remember { mutableStateOf<Uri?>(null) }
//    val imagePickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent(),
//        onResult = {
//            navController.navigate("image_conversion_page?uri={uri}"
//                .replace(
//                    oldValue = "{uri}",
//                    newValue = "${Uri.encode(it.toString())}"
//                )
//            ){
//                popUpTo("home_graph") { inclusive = false}
//                launchSingleTop = false
//            }
//        }
//    )
//
//    val context = LocalContext.current
//
//    val cameraPermissionState = rememberPermissionState(
//        android.Manifest.permission.CAMERA
//    )
//
//    LaunchedEffect(Unit) {
//        if (!cameraPermissionState.status.isGranted){
//            cameraPermissionState.launchPermissionRequest()
//        }
//    }
//
//
//    val permissionState = rememberPermissionState(
//        permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            android.Manifest.permission.READ_MEDIA_IMAGES
//        } else {
//            android.Manifest.permission.READ_EXTERNAL_STORAGE
//        }
//    )
//
//    LaunchedEffect(permissionState.status) {
//        if (permissionState.status.isGranted) {
//            firstImageUri = getFirstImageFromGallery(context)
//        } else if (permissionState.status.shouldShowRationale) {
//
//        } else {
//
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        if (!permissionState.status.isGranted) {
//            permissionState.launchPermissionRequest()
//        } else {
//
//            firstImageUri = getFirstImageFromGallery(context)
//        }
//    }
//
//
//
//    LaunchedEffect(Unit) {
//        firstImageUri = getFirstImageFromGallery(context)
//        Log.d("FirstImageUri", "First Image URI: $firstImageUri")
//    }
//
//
//    BoxWithConstraints(
//        modifier = Modifier.fillMaxSize()
//            .hazeSource(hazeState)
//    ) {
//        val boxWidth = constraints.maxWidth.toFloat()
//        val boxHeight = constraints.maxHeight.toFloat()
//
//        val rotation = previewView?.display?.rotation ?: Surface.ROTATION_0
//
//        val imageCapture = remember {
//            ImageCapture.Builder()
//                .setTargetRotation(rotation)
//                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
//                .build()
//        }
//
//        Log.d("BoxWithConstraints", "boxWidth: $boxWidth, boxHeight: $boxHeight")
//
//        // Calculate rectangle bounds based on BoxWithConstraints
//        with(density) {
//            val rectLeft = (boxWidth - captureAreaWidth.toPx()) / 2f
//            val rectTop = captureAreaOffsetY.toPx()
//            val rectRight = rectLeft + captureAreaWidth.toPx()
//            val rectBottom = rectTop + captureAreaHeight.toPx()
//
//            rectangleBounds = RectF(
//                rectLeft,
//                rectTop,
//                rectRight,
//                rectBottom
//            )
//            Log.d("RectangleBounds", "rectangleBounds: $rectangleBounds")
//        }
//
//
//        CameraPreview(
//            modifier = Modifier.fillMaxSize(),
//            rectangleBounds = rectangleBounds,
//            previewViewSetter   = { previewView = it },
//            imageCapture = imageCapture,
//            onTextDetected = { detectedTextValue, boundingBox ->
//                Log.d("Camera Screen", "Text found + $detectedTextValue ")
//                val currentTime = System.currentTimeMillis()
//                if (detectedTextValue == previousDetectedText) {
//                    if (!conversionTriggered && currentTime - detectionStartTime >= 2000) {
//                        // Detected text has been the same for more than 2 seconds
//                        conversionTriggered = true
//                        // Perform conversion
//                        coroutineScope.launch {
//                            cameraViewModel.onNumberDetected(detectedTextValue)
//                        }
//                    }
//                } else {
//                    // Detected text has changed
//                    previousDetectedText = detectedTextValue
//                    detectionStartTime = currentTime
//                    conversionTriggered = false
//                }
//                detectedText = detectedTextValue
//                textPosition = boundingBox
//                clipBorderColor = Color(0xFFFFD700)
//            }
//        )
//
//        LaunchedEffect(takePhoto) {
//
//                    if (takePhoto && previewView != null) {
//
//
//                        val values = ContentValues().apply {
//                            put(MediaStore.MediaColumns.DISPLAY_NAME, "capture_${System.currentTimeMillis()}.jpg")
//                            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//                            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyConverter")
//                        }
//                        val outputOpts = ImageCapture.OutputFileOptions
//                            .Builder(context.contentResolver,
//                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                                values)
//                            .build()
//
//                        imageCapture.takePicture(
//                            outputOpts,
//                            ContextCompat.getMainExecutor(context),
//                            object : ImageCapture.OnImageSavedCallback {
//                                override fun onImageSaved(output: ImageCapture.OutputFileResults, ) {
//
//
//                                    val fullUri = output.savedUri
//                                    if (fullUri != null) {
//                                        cropAndNavigate(
//                                            context, fullUri, rectangleBounds, previewView!!, navController
//                                        )
//                                    } else {
//                                        Log.e("CameraPreview", "No URI from MediaStore!")
//                                    }
////
//                                }
//                                override fun onError(exc: ImageCaptureException) {
//                                    Log.e("CameraPreview", "Photo save failed", exc)
////
//                                }
//                            }
//                        )
//
//                    }
//            }
//
//
//
//
//        Box(modifier = Modifier
//            .fillMaxWidth()
//            .padding(top = 30.dp)
//            .zIndex(1f)
//        ) {
//            if (converterUIStateCamera.isLoading) {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator()
//                }
//            } else {
//                DropdownMenuItemRow(
//                    currencyOptions = CurrencyOptionsData.options,
//                    selectedCurrencyFrom = converterUIStateCamera.selectedCurrencyFrom,
//                    selectedCurrencyTo = converterUIStateCamera.selectedCurrencyTo,
//                    onCurrencyFromChange = { newCurrency ->
//                        cameraViewModel.onCurrencyFromChange(newCurrency)
//                    },
//                    onCurrencyToChange = { newCurrency ->
//                        cameraViewModel.onCurrencyToChange(newCurrency)
//                    },
//                    onSwitchCurrencies = {
//                        cameraViewModel.switchCurrencies()
//                    },
//                    modifier = Modifier.zIndex(3f)
//                )
//            }
//        }
//
//
//        Box(
//            Modifier.fillMaxSize()
//        ) {
//
//            TransparentClipLayout(
//                modifier = Modifier.fillMaxSize().zIndex(1f),
//                width = captureAreaWidth,
//                height = captureAreaHeight,
//                offsetY = captureAreaOffsetY,
//                color = clipBorderColor
//            )
//
//            if (converterUIStateCamera.detectedNumber != null && converterUIStateCamera.conversionResult.isNotEmpty())  {
//                Log.d("Camera Screen", "if statment is triggered but no fucking text display ")
//                Box(
//                    modifier = Modifier
//                        .align(Alignment.Center)
//                        .padding(top = 100.dp)
//
//                ) { Box(
//                    modifier = Modifier
//                        .background(Color.Gray.copy(alpha = 0.7f))
//                        .clip(RoundedCornerShape(40.dp))
//
//                ) {
//                    Text(
//                        text = "${converterUIStateCamera.selectedCurrencyTo.uppercase()}: ${converterUIStateCamera.conversionResult}",
//                        color = Color.White,
//                        style = MaterialTheme.typography.headlineLarge
//                    )
//                }
//                }
//            }
//
//        }
//
//        Box(
//            modifier = Modifier
//                .wrapContentSize()
//                .padding(
//                    bottom = 140.dp,
//                    start = 30.dp
//                )
//                .background(
//                    Color.Gray,
//                    shape = RoundedCornerShape(15.dp)
//                )
//                .align(Alignment.BottomStart)
//                .clickable {
//                    imagePickerLauncher.launch("image/*")
//                },
//            contentAlignment = Alignment.Center
//        ) {
//
//
//                if (firstImageUri != null) {
//                    AsyncImage(
//                        model = ImageRequest.Builder(LocalContext.current)
//                            .data(firstImageUri)
//                            .crossfade(true)
//                            .build(),
//                        contentDescription = "First image from gallery",
//                        modifier = Modifier
//                            .size(85.dp)
//                            .clip(RoundedCornerShape(15.dp)),
//                        contentScale = ContentScale.Crop
//                    )
//                }
//            }
//    }
//
//    if (detectedText != previousDetectedText) {
//        conversionTriggered = false
//    }
//}
//
//
//
//
//suspend fun getFirstImageFromGallery(context: android.content.Context): Uri? {
//    return withContext(Dispatchers.IO) {
//        val projection = arrayOf(
//            MediaStore.Images.Media._ID
//        )
//        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
//
//        val query = context.contentResolver.query(
//            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//            projection,
//            null,
//            null,
//            sortOrder
//        )
//
//        query?.use { cursor ->
//            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
//
//            if (cursor.moveToFirst()) {
//                val id = cursor.getLong(idColumn)
//                return@withContext ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
//            }
//        }
//        return@withContext null
//    }
//}
//
//
//@Composable
//fun DropdownMenuItemRow(
//    currencyOptions: List<Pair<String, String>>,
//    selectedCurrencyFrom: String,
//    selectedCurrencyTo: String,
//    onCurrencyFromChange: (String) -> Unit,
//    onCurrencyToChange: (String) -> Unit,
//    onSwitchCurrencies: () -> Unit,
//    modifier: Modifier = Modifier) {
//
//    var isSwitched by remember { mutableStateOf(false) }
//
//    val dropdownSize by animateDpAsState(targetValue = if (isSwitched) 70.dp else 56.dp)
//
//
//    Row(
//        modifier = modifier.padding(horizontal = 32.dp)
//    ) {
//
//        Box(modifier =  Modifier.weight(1f)
//            .padding(
//                start = 10.dp,
//                end = 10.dp
//            )
//            .height(dropdownSize)) {
//
//            DropdownMenuSpinner(
//                optionsList = currencyOptions,
//                selectedCurrency = selectedCurrencyFrom,
//                onCurrencySelected = onCurrencyFromChange,
//                backgroundColor = Color(0xFFFD5B66),
//                textColor = Color.White,
//                trailingColor = Color.White,
//                borderColor = Color.Transparent,
//            )
//        }
//
//        IconButton(
//            onClick = {
//                onSwitchCurrencies()
//                isSwitched = !isSwitched
//            },
//        ) {
//            Icon(
//                painter = painterResource(R.drawable.switch_sides_button),
//                contentDescription = "Switch Currency Place",
//                tint = Color(0xFFFD5B66)
//            )
//        }
//
//        Box(modifier = Modifier
//            .weight(1f)
//            .padding(
//                start = 10.dp,
//                end = 10.dp
//            )
//            .height(dropdownSize)
//        ) {
//            DropdownMenuSpinner(
//                optionsList = currencyOptions,
//                selectedCurrency = selectedCurrencyTo,
//                onCurrencySelected = onCurrencyToChange,
//                backgroundColor = Color(0xFFFD5B66),
//                textColor = Color.White,
//                trailingColor = Color.White,
//                borderColor = Color.Transparent,
//            )
//        }
//    }
//}




@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewScreen(
    cameraViewModel: CameraViewModel = hiltViewModel(),
    hazeState: HazeState,
    navController: NavController,
    takePhoto: Boolean
) {

    Log.d("CameraPreviewScreen", "take photo: $takePhoto")

    val converterUIStateCamera by cameraViewModel.converterUIState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var previousDetectedText by remember { mutableStateOf("") }
    var detectionStartTime by remember { mutableStateOf(0L) }
    var conversionTriggered by remember { mutableStateOf(false) }



    // State for detected text and its position
    var detectedText by remember { mutableStateOf("") }
    var textPosition by remember { mutableStateOf<android.graphics.Rect?>(null) }

    val captureAreaWidth = 300.dp
    val captureAreaHeight = 200.dp
    val captureAreaOffsetY = 150.dp
    var clipBorderColor by remember { mutableStateOf(Color.Gray) }
    var rectangleBounds by remember { mutableStateOf(RectF()) }

    val density = LocalDensity.current


    var previewView by remember { mutableStateOf<PreviewView?>(null) }


    var firstImageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = {
            navController.navigate("image_conversion_page?uri={uri}"
                .replace(
                    oldValue = "{uri}",
                    newValue = "${Uri.encode(it.toString())}"
                )
            ){
                popUpTo("home_graph") { inclusive = false}
                launchSingleTop = false
            }
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

        val rotation = previewView?.display?.rotation ?: Surface.ROTATION_0

        val imageCapture = remember {
            ImageCapture.Builder()
                .setTargetRotation(rotation)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()
        }

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
            rectangleBounds = rectangleBounds,
            previewViewSetter   = { previewView = it },
            imageCapture = imageCapture,
            onTextDetected = { detectedTextValue, boundingBox ->
                Log.d("Camera Screen", "Text found + $detectedTextValue ")
                val currentTime = System.currentTimeMillis()
                if (detectedTextValue == previousDetectedText) {
                    if (!conversionTriggered && currentTime - detectionStartTime >= 2000) {
                        conversionTriggered = true
                        coroutineScope.launch {
                            cameraViewModel.onNumberDetected(detectedTextValue)
                        }
                    }
                } else {
                    previousDetectedText = detectedTextValue
                    detectionStartTime = currentTime
                    conversionTriggered = false
                }
                detectedText = detectedTextValue
                textPosition = boundingBox
                clipBorderColor = Color(0xFFFFD700)
            }
        )

        LaunchedEffect(takePhoto) {

            if (takePhoto && previewView != null) {

                imageCapture.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {

                        override fun onCaptureSuccess(imageProxy: ImageProxy) {
                            coroutineScope.launch(Dispatchers.IO) {
                                // 1) convert the proxy to a Bitmap
                                val fullBmp = imageProxy.toBitmap()

                                // 2) crop the bitmap so it matches `rectangleBounds`
                                val cropBmp = cropBitmapFromPreview(
                                    fullBmp,
                                    rectangleBounds,
                                    previewView!!
                                )

                                // 3) save the cropped bitmap and get its Uri back
                                val croppedUri = saveBitmapToGallery(
                                    context,
                                    cropBmp,
                                    displayNamePrefix = "crop_"
                                )

                                // 4) close & recycle
                                imageProxy.close()
                                cropBmp.recycle()

                                // 5) navigate on the main thread
                                withContext(Dispatchers.Main) {
                                    navController.navigate(
                                        "image_conversion_page?uri=${Uri.encode(croppedUri.toString())}"
                                    ) {
                                        popUpTo("home_graph") { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }

                        override fun onError(exc: ImageCaptureException) {
                            Log.e("CameraPreview", "Capture error", exc)
                        }
                    }
                )

            }
        }




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

fun cropBitmapFromPreview(
    source: Bitmap,
    rectF: RectF,
    preview: PreviewView
): Bitmap {
    val vw = preview.width.toFloat()
    val vh = preview.height.toFloat()
    val iw = source.width.toFloat()
    val ih = source.height.toFloat()
    val viewAR = vw / vh
    val imgAR  = iw / ih

    val scale: Float
    val offsetX: Float
    val offsetY: Float

    if (viewAR > imgAR) {
        scale     = ih / vh
        offsetX   = (vw - ih * imgAR) / 2f
        offsetY   = 0f
    } else {
        scale     = iw / vw
        offsetX   = 0f
        offsetY   = (vh - iw / imgAR) / 2f
    }

    val left   = ((rectF.left   - offsetX)).toInt().coerceIn(0, source.width)
    val top    = ((rectF.top    - offsetY)).toInt().coerceIn(0, source.height)
    val right  = ((rectF.right  - offsetX) * scale).toInt().coerceIn(0, source.width)
    val bottom = ((rectF.bottom - offsetY) * scale).toInt().coerceIn(0, source.height)

    return Bitmap.createBitmap(source, left, top, right - left, bottom - top)
}


fun saveBitmapToGallery(
    context: Context,
    bmp: Bitmap,
    displayNamePrefix: String = "img_"
): Uri {
    val cv = ContentValues().apply {
        put(
            MediaStore.MediaColumns.DISPLAY_NAME,
            "${displayNamePrefix}${System.currentTimeMillis()}.jpg"
        )
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(
            MediaStore.Images.Media.RELATIVE_PATH,
            "${Environment.DIRECTORY_PICTURES}/MyConverter"
        )
    }
    val uri = context.contentResolver
        .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)!!

    context.contentResolver.openOutputStream(uri)?.use { out ->
        bmp.rotate(90f).compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return uri
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


fun cropAndNavigate(
    context: Context,
    fullUri: Uri,
    rectF: RectF,
    previewView: PreviewView,
    navController: NavController
) {
    context.contentResolver.openInputStream(fullUri)?.use { input ->
        val decoder = BitmapRegionDecoder.newInstance(input, false)
            ?: return Log.e("CameraPreview", "Decoder null").let { }


        val vw = previewView.width.toFloat()
        val vh = previewView.height.toFloat()
        val iw = decoder.width.toFloat()
        val ih = decoder.height.toFloat()
        val viewAR = vw / vh
        val imgAR  = iw / ih

        val scale: Float
        val offsetX: Float
        val offsetY: Float

        if (viewAR > imgAR) {
            // view is wider ⇒ image is fit height and cropped horizontally
            scale   = ih / vh
            val displayedW = ih * imgAR    // how wide the image actually is in view-px
            offsetX = (vw - displayedW) / 2f
            offsetY = 0f
        } else {
            // view is taller ⇒ image is fit width and cropped vertically
            scale   = iw / vw
            val displayedH = iw / imgAR
            offsetX = 0f
            offsetY = (vh - displayedH) / 2f
        }

        // 2) map your view rectangle → JPEG coordinates
        val left   = ((rectF.left   - offsetX)).toInt().coerceIn(0, decoder.width)
        val top    = (rectF.top    - offsetY).toInt().coerceIn(0, decoder.height)
        val right  = ((rectF.right  - offsetX) * scale).toInt().coerceIn(0, decoder.width)
        val bottom = ((rectF.bottom - offsetY) * scale).toInt().coerceIn(0, decoder.height)
        val cropRect = Rect(left, top, right, bottom)

        // 3) decode only that region
        val regionBmp = decoder.decodeRegion(cropRect, null)
        decoder.recycle()

        // 4) save & navigate (same as before)
        val cv = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "crop_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/MyConverter")
        }
        val outUri = context.contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)!!
        context.contentResolver.openOutputStream(outUri)?.use { os ->
            regionBmp.compress(Bitmap.CompressFormat.JPEG, 90, os)
        }

        navController.navigate("image_conversion_page?uri=${Uri.encode(outUri.toString())}") {
            popUpTo("home_graph") { inclusive = false }
            launchSingleTop = true
        }
    }
}


fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

