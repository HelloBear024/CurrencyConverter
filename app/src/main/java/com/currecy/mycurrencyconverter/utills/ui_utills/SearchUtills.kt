package com.currecy.mycurrencyconverter.utills.ui_utills

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials


@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GlassmorphicContainerSearchbar(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.ultraThin(),
            ){
                blurRadius = 20.dp
            },
    ) {
        content()
    }
}