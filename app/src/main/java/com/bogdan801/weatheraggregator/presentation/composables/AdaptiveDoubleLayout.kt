package com.bogdan801.weatheraggregator.presentation.composables

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AdaptiveDoubleLayout(
    modifier: Modifier = Modifier,
    isPortrait: Boolean? = null,
    ratio: Float = 1f,
    horizontalRatio: Float = ratio,
    firstPart: @Composable (BoxScope.() -> Unit),
    secondPart: @Composable (BoxScope.() -> Unit)
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        if (isPortrait ?: (maxHeight > maxWidth)){
            Column(modifier = Modifier.fillMaxSize()){
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                    contentAlignment = Alignment.Center
                ){
                    firstPart(this)
                }
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f/ratio),
                    contentAlignment = Alignment.Center
                ){
                    secondPart(this)
                }
            }
        }
        else {
            Row(
                modifier = Modifier.fillMaxSize()
            ){
                Box(modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                    contentAlignment = Alignment.Center
                ){
                    firstPart(this)
                }
                Box(modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f/horizontalRatio),
                    contentAlignment = Alignment.Center
                ){
                    secondPart(this)
                }
            }
        }
    }
}