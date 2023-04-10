package com.bogdan801.weatheraggregator.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.bogdan801.weatheraggregator.domain.model.Location
import com.bogdan801.weatheraggregator.domain.model.WeatherSourceDomain

@Composable
fun SelectDataSourcesSheet(
    modifier: Modifier = Modifier,
    location: Location,
    onSourcesSelected: (List<WeatherSourceDomain>) -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = "${location.oblastName}/${location.regionName}/${location.name}", color = Color.Black)
    }
}