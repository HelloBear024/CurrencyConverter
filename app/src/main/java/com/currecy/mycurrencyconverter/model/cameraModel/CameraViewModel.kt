package com.currecy.mycurrencyconverter.model.cameraModel

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import android.util.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.currecy.mycurrencyconverter.database.preferencess.camera.CameraPagePreferencesRepository
import com.currecy.mycurrencyconverter.database.preferencess.currencyRates.CurrencyRateDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val currencyDao: CurrencyRateDao,
    private val preferencesRepository: CameraPagePreferencesRepository,
//    private val cropper: ImageCropper
): ViewModel() {

    //new Implimentation
    private val _fullBitmap = MutableStateFlow<Bitmap?>(null)
    val fullBitmap: StateFlow<Bitmap?> = _fullBitmap



    private val _converterUIState = MutableStateFlow(CurrencyCameraUIState())
    val converterUIState: StateFlow<CurrencyCameraUIState> = _converterUIState.asStateFlow()


    init {
        viewModelScope.launch {
            fetchPreferencesAndUpdateState()
        }
    }

//    suspend fun processImage(imageProxy: ImageProxy, viewSize: Size, cropRect: RectF) {
//        val bitmap = imageProxy.toBitmap() // extension you already have
//        val cropped = cropper.cropFromViewRect(
//            fullBitmap   = bitmap,
//            viewSize     = viewSize,
//            destSize     = Size(bitmap.width, bitmap.height),
//            cropRectOnView = cropRect
//        )
//        saveAndNavigate(cropped)
//        bitmap.recycle()
//    }


    private suspend fun fetchPreferencesAndUpdateState() {
        try {
            val prefs = preferencesRepository.getPreferences()

            // Update the UI state with the loaded preferences
            _converterUIState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    selectedCurrencyFrom = prefs.firstCurrency.ifEmpty { "usd" }, // Fallback to "usd"
                    selectedCurrencyTo = prefs.secondCurrency.ifEmpty { "eur" }   // Fallback to "eur"
                )
            }
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Error fetching preferences", e)
            _converterUIState.update { currentState ->
                currentState.copy(isLoading = false)
            }
        }
    }

    // Called when new text is detected from the camera
    suspend fun onNumberDetected(detectedText: String) {
        val number = detectedText.toDoubleOrNull()
        if (number != null) {
            _converterUIState.update { it.copy(detectedNumber = number) }
            convertCurrency()
        }
    }

     fun onCurrencyFromChange(newCurrency: String) {
        viewModelScope.launch {
            _converterUIState.update { it.copy(selectedCurrencyFrom = newCurrency) }
            preferencesRepository.updateCurrencies(newCurrency, _converterUIState.value.selectedCurrencyTo)
            convertCurrency()
        }
    }

     fun onCurrencyToChange(newCurrency: String) {
        viewModelScope.launch {
            _converterUIState.update { it.copy(selectedCurrencyTo = newCurrency) }
            preferencesRepository.updateCurrencies(_converterUIState.value.selectedCurrencyFrom, newCurrency)
            convertCurrency()
        }
    }

    fun switchCurrencies() {
        viewModelScope.launch {
            val currentFrom = _converterUIState.value.selectedCurrencyFrom
            val currentTo = _converterUIState.value.selectedCurrencyTo
            _converterUIState.update { currentState ->
                currentState.copy(
                    selectedCurrencyFrom = currentTo,
                    selectedCurrencyTo = currentFrom
                )
            }
            // Update preferences and recalculate conversion
            preferencesRepository.updateCurrencies(currentTo, currentFrom)
            convertCurrency()
        }
    }


     suspend fun convertCurrency() {
         val fromCurrency = _converterUIState.value.selectedCurrencyFrom
         val toCurrency = _converterUIState.value.selectedCurrencyTo
         val amount = _converterUIState.value.detectedNumber ?: return

         val fromRate = currencyDao.getRateForCurrency(fromCurrency)
         val toRate = currencyDao.getRateForCurrency(toCurrency)

         if (fromRate == null || toRate == null) {
             Log.d("CurrencyConversion", "Could not retrieve rates. From Rate or To Rate is null.")
         }

         if (fromRate != null && toRate != null) {
             var convertedAmount = amount * (toRate/ fromRate)
             convertedAmount = formatToTwoDecimals(convertedAmount)
             _converterUIState.update { it.copy(conversionResult = convertedAmount.toString()) }
         } else {
             _converterUIState.update { it.copy(conversionResult = "Conversion failed") }
         }
     }


    private fun formatToTwoDecimals(value: Double): Double {
        return "%.2f".format(value).toDouble()
    }
}

interface ImageCropper {
    /**
     * Map the rectangle in **view** coordinates into the full‐size bitmap, then
     * return a cropped bitmap of that region.
     *
     * @param fullBitmap the camera capture at sensor resolution.
     * @param viewSize   the size of the PreviewView on screen (px).
     * @param destSize   the size of the fullBitmap (px).
     * @param cropRectOnView the rectangle drawn on the PreviewView (px).
     */
    fun cropFromViewRect(
        fullBitmap: Bitmap,
        viewSize: Size,
        destSize: Size,
        cropRectOnView: RectF
    ): Bitmap
}

class MatrixImageCropper : ImageCropper {
    override fun cropFromViewRect(
        fullBitmap: Bitmap,
        viewSize: Size,
        destSize: Size,
        cropRectOnView: RectF
    ): Bitmap {
        // 1) Build a matrix that maps view→bitmap
        val viewRect = RectF(0f, 0f, viewSize.width.toFloat(), viewSize.height.toFloat())
        val bmpRect  = RectF(0f, 0f, destSize.width.toFloat(), destSize.height.toFloat())
        val m = Matrix().apply {
            // FILL is correct for PreviewView’s default ScaleType (CENTER_CROP)
            setRectToRect(viewRect, bmpRect, Matrix.ScaleToFit.FILL)
            invert(this)
        }

        // 2) Transform the cropRect corners
        val pts = floatArrayOf(
            cropRectOnView.left,  cropRectOnView.top,
            cropRectOnView.right, cropRectOnView.bottom
        )
        m.mapPoints(pts)

        // 3) Clamp and convert to ints
        val left   = pts[0].coerceIn(0f, destSize.width.toFloat()).toInt()
        val top    = pts[1].coerceIn(0f, destSize.height.toFloat()).toInt()
        val right  = pts[2].coerceIn(0f, destSize.width.toFloat()).toInt()
        val bottom = pts[3].coerceIn(0f, destSize.height.toFloat()).toInt()

        return Bitmap.createBitmap(fullBitmap, left, top, right - left, bottom - top)
    }
}





