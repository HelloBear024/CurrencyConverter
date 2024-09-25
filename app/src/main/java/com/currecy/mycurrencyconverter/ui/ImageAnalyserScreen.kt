package com.currecy.mycurrencyconverter.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ImageAnalyserScreen(selectedImageUri: Uri?) {

    Box(
    modifier = Modifier
    .fillMaxSize()
    .background(Color.Gray),
    contentAlignment = Alignment.Center
    ) {
        if (selectedImageUri != null) {
            // If an image is selected, display it
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(selectedImageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Maintains aspect ratio
                   , // Rounded corners
                contentScale = ContentScale.Crop
            )
        } else {
            // If no image is selected, display a placeholder or message
            Text(
                text = "No image selected",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }


}