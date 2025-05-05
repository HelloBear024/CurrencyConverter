package com.currecy.mycurrencyconverter.ui

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.room.util.copy
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.currecy.mycurrencyconverter.R
import com.currecy.mycurrencyconverter.data.CurrencyOptionsData
import com.currecy.mycurrencyconverter.model.cameraModel.CameraViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.min
import android.graphics.Rect as AndroidRect
import androidx.compose.ui.geometry.Rect as ComposeRect

@Composable
fun ImageConversionScreen(
    hazeState: HazeState,
    imageUri: Uri,
    viewModel: CameraViewModel = hiltViewModel(),
) {


    // newImplimentations



    val converterUIState by viewModel.converterUIState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var selectedNumber by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { _ ->

        AsyncImage(
            model = R.drawable.background,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
        )


        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {

            val maxWith = this.maxWidth
            val maxHeight = this.maxHeight

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp)
            ) {

                DropdownMenuItemRow(
                    currencyOptions = CurrencyOptionsData.options,
                    selectedCurrencyFrom = converterUIState.selectedCurrencyFrom,
                    selectedCurrencyTo = converterUIState.selectedCurrencyTo,
                    onCurrencyFromChange = { newCurrency ->

                        viewModel.onCurrencyFromChange(newCurrency)
                    },
                    onCurrencyToChange = { newCurrency ->
                        viewModel.onCurrencyToChange(newCurrency)
                    },
                    onSwitchCurrencies = {
                        viewModel.switchCurrencies()
                    }
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                    .align(Alignment.BottomCenter)
                    .height(maxHeight / 1.15f)
                    .width(maxWith)
                    .hazeEffect(
                        state = hazeState,
                        style = HazeMaterials.ultraThin()
                    ) {
                        blurRadius = 30.dp
                        noiseFactor
                    },
            ) {

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .height(maxHeight / 1.2f)
                        .width(maxWith - 20.dp)
                        .align(Alignment.Center)
                        .zIndex(2f)
                ) {
                    PinchToZoomView(
                        imageContentDescription = "",
                        imageUri = imageUri,
                        modifier = Modifier.fillMaxSize(),
                        onNumberDetected = {
                            newNumber -> selectedNumber = newNumber
                            coroutineScope.launch {
                            viewModel.onNumberDetected(selectedNumber!!)
                            }
                        }
                    )
                }

                if (selectedNumber != null && converterUIState.conversionResult.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .zIndex(5f)
                            .clip(RoundedCornerShape(12.dp))
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 150.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = "${converterUIState.selectedCurrencyTo.uppercase()}: ${converterUIState.conversionResult}",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier
                                .background(Color.Gray.copy(alpha = 0.7f))
                                .padding(16.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                }
            }
        }
    }
}


suspend fun performTextRecognitionOnImage(
    context: Context,
    uri: Uri,
    onTextRecognized: (List<Pair<String, ComposeRect>>) -> Unit
) {
    val image: InputImage
    try {
        image = InputImage.fromFilePath(context, uri)

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val visionText = recognizer.process(image).await()

        val textWithBoundingBoxes = mutableListOf<Pair<String, ComposeRect>>()
        Log.d("TextRecognition", "Starting text recognition")
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    val text = element.text
                    val boundingBox: AndroidRect? = element.boundingBox
                    if (boundingBox != null) {
                        val composeRect = ComposeRect(
                            left = boundingBox.left.toFloat(),
                            top = boundingBox.top.toFloat(),
                            right = boundingBox.right.toFloat(),
                            bottom = boundingBox.bottom.toFloat()
                        )
                        textWithBoundingBoxes.add(Pair(text, composeRect))
                        Log.d("TextRecognition", "Detected text: $text, BoundingBox: $composeRect")
                    }
                }
            }
        }
        withContext(Dispatchers.Main) {
            onTextRecognized(textWithBoundingBoxes)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun PinchToZoomView(
    modifier: Modifier = Modifier,
    imageContentDescription: String = "",
    imageUri: Uri,
    onNumberDetected: (String) -> Unit,
) {
    val context = LocalContext.current

    val source = ImageDecoder.createSource(context.contentResolver, imageUri)
    val (srcW, srcH) = ImageDecoder.decodeBitmap(source) { c, _, _ ->
        c.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        c.isMutableRequired = false
        c.setTargetSampleSize(1)      // no down‑sample, we just want bounds
    }.let { it.width to it.height }


    var dstSize by remember { mutableStateOf(IntSize.Zero) }


    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val minScale = 1f
    val maxScale = 4f

    val metrics = remember(srcW, srcH, dstSize) {
        ImageMetrics(srcW, srcH, dstSize.width.toFloat(), dstSize.height.toFloat())
    }

    var originalBoundingBoxes by remember {
        mutableStateOf<List<Pair<String, ComposeRect>>>(emptyList())
    }

    LaunchedEffect(imageUri) {
        performTextRecognitionOnImage(context, imageUri) { detectedTexts ->
            originalBoundingBoxes = detectedTexts
        }
    }

    val transformState = rememberTransformableState { zoom, pan, _ ->
        scale = (scale * zoom).coerceIn(minScale, maxScale)
        offsetX += pan.x
        offsetY += pan.y
    }

    Box(
        modifier = modifier
//            .background(Color(0x4DFFFFFF))
            .fillMaxSize()
            .transformable(state = transformState)
            .pointerInput(originalBoundingBoxes, scale, offsetX, offsetY) {
                detectTapGestures { tapOffset ->
                    val srcPt = metrics.unmap(tapOffset, offsetX, offsetY)
                    originalBoundingBoxes
                        .firstOrNull { it.second.contains(srcPt) }
                        ?.let { (text, _) ->
                            if (text.toDoubleOrNull() != null) onNumberDetected(text)
                        }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = imageContentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().onGloballyPositioned { dstSize = it.size }
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                originalBoundingBoxes.forEach { (text, rect) ->

                    val mapped = metrics.map(rect)
                    val color = if (text.toDoubleOrNull() != null) {
                        Color.Green.copy(alpha = 0.4f)
                    } else {
                        Color.Red.copy(alpha = 0.4f)
                    }
                    drawRect(
                        color = color,
                        topLeft = Offset(mapped.left, mapped.top),
                        size = Size(mapped.width, mapped.height),
                        style = Fill
                    )
                }
            }
        }
    }
}

data class ImageMetrics(
    val srcW: Int,
    val srcH: Int,
    val dstW: Float,
    val dstH: Float
) {
    private val scale = min(dstW / srcW, dstH / srcH)
    private val dx    = (dstW - srcW * scale) / 2f
    private val dy    = (dstH - srcH * scale) / 2f

    fun map(r: ComposeRect): ComposeRect = ComposeRect(
        left   = r.left   * scale + dx,
        top    = r.top    * scale + dy,
        right  = r.right  * scale + dx,
        bottom = r.bottom * scale + dy
    )
    fun unmap(p: Offset, offsetX: Float, offsetY: Float) = Offset(
        x = (p.x - offsetX - dx)/scale,
        y = (p.y - offsetY - dy)/scale
    )
}


