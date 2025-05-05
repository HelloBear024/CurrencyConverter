package com.currecy.mycurrencyconverter.utills.ui_utills

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.currecy.mycurrencyconverter.R
import com.currecy.mycurrencyconverter.navigation.AppScreen
import com.currecy.mycurrencyconverter.ui.BottomNavigation.Screen
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials


@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    navBarHeight: Dp = 54.dp,
    selectedScreen: AppScreen = AppScreen.HomePage,
    onScreenSelected: (AppScreen) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    takePhoto: (Boolean) -> Unit
) {

    val iconSize = when {
        navBarHeight < 70.dp -> 24.dp
        navBarHeight < 90.dp -> 28.dp
        else -> 32.dp
    }
    val fabSize = 50.dp
    val fabMargin = 8.dp
    val cutoutRadius = with(LocalDensity.current) { (fabSize / 2 + fabMargin).toPx() }
    val cornerRadius = with(LocalDensity.current) { 24.dp.toPx()}


    val fabScale by animateFloatAsState(
        targetValue = if (selectedScreen == AppScreen.CameraConversionPage) 1.1f else 1f
    )

    val fabOffsetY by animateDpAsState(
        targetValue = if (selectedScreen == AppScreen.CameraConversionPage) (-30).dp else (-30 ).dp
    )

    Box(
        modifier = modifier.fillMaxWidth().padding(bottom = 20.dp),
    ) {
        FloatingActionButton(
            onClick = {
                if (selectedScreen != AppScreen.CameraConversionPage) {
                    onScreenSelected(AppScreen.CameraConversionPage)
                } else {
                    takePhoto(true)
                }
            },
            containerColor = Color.White,
            contentColor = if (selectedScreen == AppScreen.CameraConversionPage)
                Color(0xFFFD5B66)
            else
                Color(0xFF1F1F1F),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = fabOffsetY)
                .size(50.dp * fabScale)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.photo),
                contentDescription = "Camera",
                modifier = Modifier.size(iconSize)
            )
        }

        Surface(
            shape = Screen(cutoutRadius, cornerRadius),
            modifier = Modifier
                .padding(horizontal = 60.dp)
                .clip(RoundedCornerShape(30.dp))
                .fillMaxWidth()
                .height(navBarHeight),
            color = Color.Transparent,
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .fillMaxSize()
                    .hazeEffect(
                        state = hazeState,
                        style = HazeMaterials.ultraThin(containerColor = Color(0x99FD5B66))
                    ){
                        blurRadius = 30.dp
                        noiseFactor
                    },
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NavigationIcon(
                        isSelected = selectedScreen == AppScreen.HomePage,
                        onClick = {
                            if (selectedScreen != AppScreen.HomePage) {
                                onScreenSelected(AppScreen.HomePage)
                            }
                        },
                        icon = Icons.Default.EuroSymbol,
                        iconSize = iconSize,
                        contentDescription = "Home",
                        defaultTint = Color(0xFF1F1F1F),
                        selectedTint = Color.White
                    )

                    Spacer(Modifier.width(60.dp))

                    NavigationIcon(
                        isSelected = selectedScreen == AppScreen.SearchChartPage,
                        onClick = {
                            if (selectedScreen != AppScreen.SearchChartPage) {
                                onScreenSelected(AppScreen.SearchChartPage)
                            }
                        },
                        icon = Icons.Default.Analytics,
                        iconSize = iconSize,
                        contentDescription = "Charts",
                        defaultTint = Color(0xFF1F1F1F),
                        selectedTint = Color.White
                    )
                }
            }
        }
    }
}


@Composable
fun NavigationIcon(
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    iconSize: Dp,
    defaultTint: Color,
    selectedTint: Color
) {
    val size by animateDpAsState(
        targetValue = if (isSelected) iconSize * 1.2f else iconSize
    )
    val tint by animateColorAsState(
        targetValue = if (isSelected) selectedTint else defaultTint
    )

    IconButton(
        onClick = onClick,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size)
        )
    }
}