package com.bogdan801.weatheraggregator.presentation.composables.repeatable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bogdan801.weatheraggregator.presentation.screens.home.WeatherDataState

@Composable
fun DataSourceCard(
    modifier: Modifier = Modifier,
    dataState: WeatherDataState,
    isSelected: Boolean = false,
    onLongPress: () -> Unit = {},
    onTap: (Boolean) -> Unit ={}
) {
    Box(modifier = modifier){
        Card(
            modifier = Modifier.fillMaxSize(),
            backgroundColor = MaterialTheme.colors.secondary,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, Color.White.copy(0.2f))
        ) {

        }
    }

}