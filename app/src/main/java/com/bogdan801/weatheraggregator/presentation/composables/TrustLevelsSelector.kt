package com.bogdan801.weatheraggregator.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.bogdan801.weatheraggregator.presentation.screens.home.WeatherDataState

@Composable
fun TrustLevelsSelector(
    modifier: Modifier = Modifier,
    dataStateList: List<WeatherDataState>,
    levels: List<Double>,
    onLevelChanged: (List<Double>) -> Unit
) {
    Row(modifier = modifier.clip(MaterialTheme.shapes.small)) {
        dataStateList.forEachIndexed { index, weatherDataState ->
            if(index!=0) {
                Column(
                    modifier = Modifier
                        .background(
                            if ((index-1) % 2 == 0) MaterialTheme.colors.onPrimary
                            else MaterialTheme.colors.onPrimary.copy(0.6f)
                        )
                        .fillMaxHeight()
                        .weight(levels[index-1].toFloat()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(
                        text = weatherDataState.data.domain.name,
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.onSurface
                    )
                    Text(
                        text = "${"%,.0f".format((levels[index-1] * 100))}%",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(0.65f)
                    )
                }
            }
        }
    }
}