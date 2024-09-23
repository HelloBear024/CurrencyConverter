package com.currecy.mycurrencyconverter.ui

import android.graphics.RectF
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import androidx.camera.core.Preview as CameraPreview

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onTextDetected: (String, android.graphics.Rect?) -> Unit,
    rectangleBounds: RectF // Pass the transparent rectangle bounds here
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize()
    ) { view ->
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val preview = CameraPreview.Builder().build().also {
                it.setSurfaceProvider(view.surfaceProvider)
            }

            // Image analysis use case for text recognition
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                        processImageProxy(imageProxy, onTextDetected, rectangleBounds)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e("CameraPreview", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }
}




@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    onTextDetected: (String, android.graphics.Rect?) -> Unit,
    rectangleBounds: RectF // Receive the rectangle bounds here
) {
    val mediaImage = imageProxy.image ?: return
    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    recognizer.process(inputImage)
        .addOnSuccessListener { visionText ->
            if (visionText.textBlocks.isNotEmpty()) {
                val detectedText = visionText.text
                val boundingBox = visionText.textBlocks.first().boundingBox

                // Check if the bounding box falls within the transparent rectangle
                if (boundingBox != null && isInsideRectangle(boundingBox, rectangleBounds)) {
                    onTextDetected(detectedText, boundingBox) // Pass the bounding box
                }
            }
        }
        .addOnFailureListener { e ->
            Log.e("CameraPreview", "Text recognition failed", e)
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

// Helper function to check if the bounding box is inside the transparent rectangle
private fun isInsideRectangle(boundingBox: android.graphics.Rect, rectangleBounds: RectF): Boolean {
    return boundingBox.left >= rectangleBounds.left &&
            boundingBox.right <= rectangleBounds.right &&
            boundingBox.top >= rectangleBounds.top &&
            boundingBox.bottom <= rectangleBounds.bottom
}
@Composable
fun TransparentClipLayout(
    modifier: Modifier,
    width: Dp,
    height: Dp,
    offsetY: Dp,
    color: Color
) {
    val density = LocalDensity.current

    // Convert DP to Px for the rectangle
    val offsetInPx = with(density) { offsetY.toPx() }
    val widthInPx = with(density) { width.toPx() }
    val heightInPx = with(density) { height.toPx() }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Coordinates for the rectangle (centered horizontally, offset vertically)
        val rectLeft = (canvasWidth - widthInPx) / 2
        val rectTop = offsetInPx
        val rectRight = rectLeft + widthInPx
        val rectBottom = rectTop + heightInPx

        // Draw the dimmed overlay around the rectangle
        drawRect(
            color = Color.Black.copy(alpha = 0.6f), // Dimmed transparent overlay
            size = Size(canvasWidth, canvasHeight),

            )

        // Clear the dimming inside the rectangle (normal camera view)
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(rectLeft, rectTop),
            size = Size(widthInPx, heightInPx),
            cornerRadius = CornerRadius(30f, 30f),
            blendMode = BlendMode.Src // Keep the normal camera feed inside the rectangle
        )

        // Draw a colored border around the rectangle
        drawRoundRect(
            color = color,
            topLeft = Offset(rectLeft, rectTop),
            size = Size(widthInPx, heightInPx),
            cornerRadius = CornerRadius(30f, 30f),
            style = Stroke(8f) // Border stroke
        )
    }
}



