package com.currecy.mycurrencyconverter.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.currecy.mycurrencyconverter.model.CameraViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraConversionScreen(
    cameraViewModel: CameraViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "camera_preview"
    ) {
        composable("camera_preview") {
            CameraPreviewScreen(
                cameraViewModel = cameraViewModel,
                onImageSelected = { uri ->
                    navController.navigate("image_conversion?uri=${Uri.encode(uri.toString())}")
                }
            )
        }
        composable(
            "image_conversion?uri={uri}",
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString("uri")!!
            ImageConversionScreen(
                imageUri = Uri.parse(uriString),
                viewModel = cameraViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}