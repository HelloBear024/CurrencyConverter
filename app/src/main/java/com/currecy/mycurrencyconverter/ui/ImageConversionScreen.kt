package com.currecy.mycurrencyconverter.ui

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.currecy.mycurrencyconverter.data.CurrencyOptionsData
import com.currecy.mycurrencyconverter.database.CurrencyRateDao
import com.currecy.mycurrencyconverter.model.CameraViewModel
import com.currecy.mycurrencyconverter.model.CurrencyViewModelFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.graphics.Rect as AndroidRect
import androidx.compose.ui.geometry.Rect as ComposeRect

@Composable
fun ImageConversionScreen(
    imageUri: Uri,
    currencyDao: CurrencyRateDao,
    onBack: () -> Unit
) {
    val viewModel: CameraViewModel = viewModel(
        factory = CurrencyViewModelFactory(currencyDao)
    )
    val converterUIState by viewModel.converterUIState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var textWithBoundingBoxes by remember { mutableStateOf<List<Pair<String, ComposeRect>>>(emptyList()) }
    var imageWidth by remember { mutableStateOf(1f) }
    var imageHeight by remember { mutableStateOf(1f) }
    var scaledTextWithBoundingBoxes by remember { mutableStateOf<List<ScaledTextBoundingBox>>(emptyList()) }
    var selectedNumber by remember { mutableStateOf<String?>(null) }


    // Get image dimensions
    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            val source = ImageDecoder.createSource(context.contentResolver, imageUri)
            val decoder = ImageDecoder.decodeDrawable(source)
            imageWidth = decoder.intrinsicWidth.toFloat()
            imageHeight = decoder.intrinsicHeight.toFloat()
        }
    }

    // Perform text recognition on the image
    LaunchedEffect(imageUri) {
        performTextRecognitionOnImage(
            context = context,
            uri = imageUri,
            onTextRecognized = { detectedTextList ->
                textWithBoundingBoxes = detectedTextList
                // Process numbers and perform conversion
                val numbers = detectedTextList.mapNotNull { (text, _) ->
                    text.toDoubleOrNull()
                }
                if (numbers.isNotEmpty()) {
                    // For simplicity, take the first detected number
                    coroutineScope.launch {
                        viewModel.onNumberDetected(numbers.first().toString())
                    }
                }
            }
        )
    }
    // Display the image and overlay
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Display the image
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUri)
                .crossfade(true)
                .build(),
            contentDescription = "Selected Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            alignment = Alignment.TopStart,
            onSuccess = { success ->
                val drawable = success.result.drawable
                imageWidth = drawable.intrinsicWidth.toFloat()
                imageHeight = drawable.intrinsicHeight.toFloat()
                Log.d("ImageDimensions", "Image dimensions: $imageWidth x $imageHeight")
            }
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(scaledTextWithBoundingBoxes) {
                    detectTapGestures { offset ->
                        // Handle tap at offset
                        val tappedBox =
                            scaledTextWithBoundingBoxes.firstOrNull { it.boundingBox.contains(offset) }
                        if (tappedBox != null && tappedBox.text.toDoubleOrNull() != null) {
                            selectedNumber = tappedBox.text
                            coroutineScope.launch {
                                viewModel.onNumberDetected(selectedNumber!!)
                            }
                        }
                    }
                }
        ) {

            val canvasWidth = size.width
            val canvasHeight = size.height
            Log.d("CanvasSize", "Canvas dimensions: $canvasWidth x $canvasHeight")
            val scaleX = canvasWidth / imageWidth
            val scaleY = canvasHeight / imageHeight
            Log.d("ScalingFactors", "Scaling factors: scaleX = $scaleX, scaleY = $scaleY")

            val newScaledBoundingBoxes = mutableListOf<ScaledTextBoundingBox>()

            for ((text, boundingBox) in textWithBoundingBoxes) {
                val rect = ComposeRect(
                    left = boundingBox.left * scaleX,
                    top = boundingBox.top * scaleY,
                    right = boundingBox.right * scaleX,
                    bottom = boundingBox.bottom * scaleY
                )

                newScaledBoundingBoxes.add(ScaledTextBoundingBox(text, rect))

                val color = when {
                    selectedNumber == text -> Color.Blue.copy(alpha = 0.4f)
                    text.toDoubleOrNull() != null -> Color.Green.copy(alpha = 0.4f)
                    else -> Color.Red.copy(alpha = 0.4f)
                }

                drawRect(
                    color = color,
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.width, rect.height),
                    style = Fill
                )
            }

            // Update the scaledTextWithBoundingBoxes
            scaledTextWithBoundingBoxes = newScaledBoundingBoxes

        }


        if (selectedNumber != null && converterUIState.conversionResult.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 50.dp),
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
                        coroutineScope.launch {
                            viewModel.onCurrencyFromChange(newCurrency)
                        }
                    },
                    onCurrencyToChange = { newCurrency ->
                        coroutineScope.launch {
                            viewModel.onCurrencyToChange(newCurrency)
                        }
                    },
                )
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
        // Extract text and bounding boxes
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    val text = element.text
                    val boundingBox: AndroidRect? = element.boundingBox
                    if (boundingBox != null) {
                        // Convert AndroidRect to ComposeRect
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

data class ScaledTextBoundingBox(val text: String, val boundingBox: ComposeRect)



