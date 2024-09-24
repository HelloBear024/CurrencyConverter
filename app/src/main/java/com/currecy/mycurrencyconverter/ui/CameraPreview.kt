package com.currecy.mycurrencyconverter.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.YuvImage
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.work.await
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.ByteArrayOutputStream
import kotlin.math.min
import androidx.camera.core.Preview as CameraPreview

@SuppressLint("RestrictedApi")
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

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val cameraProvider = cameraProviderFuture.await() // Use await() from kotlinx-coroutines-play-services
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        val preview = CameraPreview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        // Set up the analyzer
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
            processImageProxy(imageProxy, onTextDetected, rectangleBounds, previewView)
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, imageAnalysis
            )
        } catch (exc: Exception) {
            Log.e("CameraPreview", "Use case binding failed", exc)
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize()
    )
}


@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    onTextDetected: (String, android.graphics.Rect?) -> Unit,
    rectangleBounds: RectF,
    previewView: PreviewView
) {
    Log.d("TextRecognition", "processImageProxy called")
    val mediaImage = imageProxy.image ?: run {
        imageProxy.close()
        return
    }

    // Convert ImageProxy to Bitmap
    val bitmap = toBitmap(imageProxy)

    // Check if bitmap conversion was successful
    if (bitmap == null) {
        imageProxy.close()
        return
    }

    // Crop the bitmap to the rectangleBounds area
    val croppedBitmap = cropBitmapToRectangle(bitmap, rectangleBounds, previewView)

    // Create InputImage from the cropped bitmap
    val inputImage = InputImage.fromBitmap(croppedBitmap, 0) // No rotation needed since we already handled it

    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    recognizer.process(inputImage)
        .addOnSuccessListener { visionText ->
            if (visionText.textBlocks.isNotEmpty()) {
                val detectedText = visionText.text
                val boundingBox = visionText.textBlocks.first().boundingBox

                // Since the image is cropped, boundingBox coordinates are relative to the cropped image
                // If needed, adjust them to the preview coordinates by adding rectangleBounds.left and rectangleBounds.top

                Log.d("TextRecognition", "Detected Text: $detectedText")
                onTextDetected(detectedText, boundingBox)
            } else {
                Log.d("TextRecognition", "No text blocks detected")
            }
        }
        .addOnFailureListener { e ->
            Log.e("TextRecognition", "Text recognition failed", e)
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

@OptIn(ExperimentalGetImage::class)
private fun toBitmap(imageProxy: ImageProxy): Bitmap? {
    val image = imageProxy.image ?: return null

    val nv21 = yuv420888ToNv21(imageProxy)
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
    val imageBytes = out.toByteArray()
    var bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

    // Handle rotation
    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
    if (rotationDegrees != 0) {
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    return bitmap
}


private fun yuv420888ToNv21(imageProxy: ImageProxy): ByteArray {
    val yBuffer = imageProxy.planes[0].buffer // Y
    val uBuffer = imageProxy.planes[1].buffer // U
    val vBuffer = imageProxy.planes[2].buffer // V

    yBuffer.rewind()
    uBuffer.rewind()
    vBuffer.rewind()

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    // U and V are swapped
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)
    return nv21
}

private fun cropBitmapToRectangle(
    bitmap: Bitmap,
    rectangleBounds: RectF,
    previewView: PreviewView
): Bitmap {
    // Calculate the scaling factors between the bitmap and the previewView
    val scaleX = bitmap.width.toFloat() / previewView.width
    val scaleY = bitmap.height.toFloat() / previewView.height

    // Scale the rectangleBounds to bitmap coordinates
    val left = (rectangleBounds.left * scaleX).toInt()
    val top = (rectangleBounds.top * scaleY).toInt()
    val right = (rectangleBounds.right * scaleX).toInt()
    val bottom = (rectangleBounds.bottom * scaleY).toInt()

    // Ensure the coordinates are within the bitmap bounds
    val adjustedLeft = left.coerceIn(0, bitmap.width)
    val adjustedTop = top.coerceIn(0, bitmap.height)
    val adjustedRight = right.coerceIn(0, bitmap.width)
    val adjustedBottom = bottom.coerceIn(0, bitmap.height)

    val width = adjustedRight - adjustedLeft
    val height = adjustedBottom - adjustedTop

    return Bitmap.createBitmap(bitmap, adjustedLeft, adjustedTop, width, height)
}



private fun adjustBoundingBox(
    boundingBox: android.graphics.Rect,
    imageProxy: ImageProxy,
    previewView: PreviewView
): RectF {
    val rotationDegrees = imageProxy.imageInfo.rotationDegrees

    // Dimensions of the image
    var imageWidth = imageProxy.width.toFloat()
    var imageHeight = imageProxy.height.toFloat()

    // Swap width and height if rotated by 90 or 270 degrees
    if (rotationDegrees == 90 || rotationDegrees == 270) {
        imageWidth = imageProxy.height.toFloat()
        imageHeight = imageProxy.width.toFloat()
    }

    // Dimensions of the PreviewView
    val viewWidth = previewView.width.toFloat()
    val viewHeight = previewView.height.toFloat()

    if (viewWidth == 0f || viewHeight == 0f || imageWidth == 0f || imageHeight == 0f) {
        return RectF()
    }

    // Compute scale to fit the image into the PreviewView while preserving aspect ratio
    val scale = min(viewWidth / imageWidth, viewHeight / imageHeight)

    // Compute scaled image dimensions
    val scaledWidth = imageWidth * scale
    val scaledHeight = imageHeight * scale

    // Compute padding (letterboxing)
    val paddingLeft = (viewWidth - scaledWidth) / 2f
    val paddingTop = (viewHeight - scaledHeight) / 2f

    // Map boundingBox coordinates
    val mappedLeft = boundingBox.left * scale + paddingLeft
    val mappedTop = boundingBox.top * scale + paddingTop
    val mappedRight = boundingBox.right * scale + paddingLeft
    val mappedBottom = boundingBox.bottom * scale + paddingTop

    return RectF(mappedLeft, mappedTop, mappedRight, mappedBottom)
}



// Helper function to check if the bounding box is inside the transparent rectangle
private fun isInsideRectangle(boundingBox: RectF, rectangleBounds: RectF): Boolean {
    val result = boundingBox.left >= rectangleBounds.left &&
            boundingBox.right <= rectangleBounds.right &&
            boundingBox.top >= rectangleBounds.top &&
            boundingBox.bottom <= rectangleBounds.bottom
    Log.d("isInsideRectangle", "BoundingBox: $boundingBox, RectangleBounds: $rectangleBounds, Result: $result")
    return result
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



