package com.currecy.mycurrencyconverter.ui

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
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
import androidx.camera.core.AspectRatio
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import kotlin.math.roundToInt


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewScreen(
    cameraViewModel: CameraViewModel = hiltViewModel(),
    hazeState: HazeState,
    navController: NavController,
    takePhoto: Boolean,
    onPhotoTaken: () -> Unit
) {

    Log.d("CameraPreviewScreen", "take photo: $takePhoto")

    val converterUIStateCamera by cameraViewModel.converterUIState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var previousDetectedText by remember { mutableStateOf("") }
    var detectionStartTime by remember { mutableStateOf(0L) }
    var conversionTriggered by remember { mutableStateOf(false) }

    // State for detected text and its position
    var detectedText by remember { mutableStateOf("") }
    var textPosition by remember { mutableStateOf<Rect?>(null) }

    val captureAreaWidth = 300.dp
    val captureAreaHeight = 200.dp
    val captureAreaOffsetY = 150.dp
    var clipBorderColor by remember { mutableStateOf(Color.Gray) }
    var rectangleBounds by remember { mutableStateOf(RectF()) }


    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)

    val statusBarDp = with(density) { statusBarHeight.toDp() }

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
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(rotation)
                .setTargetRotation(Surface.ROTATION_0)
                .build()
        }

        Log.d("CameraPreviewScreen", "${imageCapture.resolutionInfo?.resolution}")

        Log.d("BoxWithConstraints", "boxWidth: $boxWidth, boxHeight: $boxHeight")

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
                                val fullBmp = imageProxy.toBitmap()

                                Log.d("Camera Prew","bitmap height = ${fullBmp.height} bitmap width = ${fullBmp.width}")
                                Log.d("Camera Prew","Phone screen height = ${boxHeight} Phone screen width = ${boxWidth}" )
                                Log.d("Camera Prew","Preview screen height = ${previewView!!.height} Preview screen width = ${previewView!!.width}" )

                                val screenWidth = previewView!!.width
                                val screenHeight = previewView!!.height

                                val cropBmp = fullBmp.rotate(90f).centerCropAndScale(screenWidth, screenHeight)

                                val left = rectangleBounds.left.roundToInt().coerceIn(0, cropBmp.width - 1)
                                val top = rectangleBounds.top.roundToInt().coerceIn(0, cropBmp.height - 1)
                                val width = rectangleBounds.width().roundToInt().coerceAtMost(cropBmp.width - left)
                                val height = rectangleBounds.height().roundToInt().coerceAtMost(cropBmp.height - top)

                                val clippedBmp = Bitmap.createBitmap(cropBmp, left, top, width, height)

                                Log.d("Camera Prew","bitmap height = ${cropBmp.height} bitmap width = ${cropBmp.width}")

                                val croppedUri = saveBitmapToGallery(
                                    context,
                                    clippedBmp,
                                    displayNamePrefix = "crop_"
                                )

                                imageProxy.close()
                                clippedBmp.recycle()

                                // 5) navigate on the main thread
                                withContext(Dispatchers.Main) {
                                    onPhotoTaken()
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
            .padding(top = 8.dp + statusBarDp)
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
                        .padding(
                            top = 100.dp
                        )

                ) {
                    Box(
                    modifier = Modifier
                        .background(Color.Gray.copy(alpha = 0.7f))
                        .clip(RoundedCornerShape(40.dp))

                ) {
                    Text(
                        text = "${ converterUIStateCamera.selectedCurrencyTo.uppercase() }: ${converterUIStateCamera.conversionResult}",
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
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
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
fun cropAndScaleToView(
    source: Bitmap,
    screenWidth: Int,
    screenHeight: Int
): Bitmap {


    Log.d("Crop Function","bitmap height = ${source.height} bitmap width = ${source.width}")
    Log.d("Crop Function","Phone screen height = ${screenHeight} Phone screen width = ${screenWidth}" )

    val bitmapWidth = source.width.toFloat()
    val bitmapHeight = source.height.toFloat()

    val screenAspectRatio = screenWidth / screenHeight.toFloat()
    val bitmapAspectRatio = bitmapWidth / bitmapHeight

    Log.d("Crop Function","Screen Aspect Ratio  = $screenAspectRatio " )

    Log.d("Crop Function","Bitmap Aspect Ratio  = $bitmapAspectRatio " )

    val scale: Float
    val scaledWidth: Float
    val scaledHeight: Float

    if (bitmapAspectRatio > screenAspectRatio) {
        scale = screenHeight / bitmapHeight
        scaledWidth = scale * bitmapWidth
        scaledHeight = screenHeight.toFloat()
    } else {
        scale = screenWidth / bitmapWidth
        scaledWidth = screenWidth.toFloat()
        scaledHeight = scale * bitmapHeight
    }


    Log.d("Crop Function","Scale   = $scale " )

    Log.d("Crop Function","Scale Width  = $scaledWidth " )
    Log.d("Crop Function","Scale Height  = $scaledHeight " )

    val dx = (scaledWidth - screenWidth) / 2.0f
    val dy = (scaledHeight - screenHeight) / 2.0f

    Log.d("Crop Function","dx   = $dx " )
    Log.d("Crop Function","dy  = $dy " )

    val matrix = Matrix().apply {
        postScale(scale, scale)
        postTranslate(-dx, -dy)
    }

    Log.d("Crop Function","matrix   = ${matrix} " )


    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        .let { Bitmap.createBitmap(it, 0, 0, screenWidth, screenHeight) }
}



fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.centerCropAndScale(targetWidth: Int, targetHeight: Int): Bitmap {
    // Compute aspect ratios
    val srcRatio    = this.width.toFloat()  / this.height.toFloat()
    val targetRatio = targetWidth.toFloat() / targetHeight.toFloat()

    // Determine the dimensions of the crop in the source bitmap
    val cropWidth: Int
    val cropHeight: Int
    if (srcRatio > targetRatio) {
        // Source is wider than target: crop width
        cropHeight = this.height
        cropWidth  = (targetRatio * cropHeight).toInt()
    } else {
        // Source is taller (or equal ratio): crop height
        cropWidth  = this.width
        cropHeight = (cropWidth / targetRatio).toInt()
    }

    // Center the crop rect
    val xOffset = (this.width  - cropWidth)  / 2
    val yOffset = (this.height - cropHeight) / 2

    // 1) Crop the region from the original
    val cropped = Bitmap.createBitmap(this, xOffset, yOffset, cropWidth, cropHeight)

    // 2) Scale the cropped bitmap to the exact target dimensions
    return Bitmap.createScaledBitmap(cropped, targetWidth, targetHeight, true)
}
