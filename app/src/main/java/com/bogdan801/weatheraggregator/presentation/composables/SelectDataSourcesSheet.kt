package com.bogdan801.weatheraggregator.presentation.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bogdan801.weatheraggregator.domain.model.Location
import com.bogdan801.weatheraggregator.domain.model.WeatherSourceDomain

@Composable
fun SelectDataSourcesSheet(
    modifier: Modifier = Modifier,
    location: Location,
    onSourcesSelected: (List<WeatherSourceDomain>) -> Unit = {}
) {
    Column(modifier = modifier){}
}