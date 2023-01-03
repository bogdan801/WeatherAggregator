package com.bogdan801.weatheraggregator.presentation.composables

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bogdan801.weatheraggregator.domain.model.SkyCondition
import com.bogdan801.weatheraggregator.domain.model.WeatherSlice
import com.bogdan801.weatheraggregator.domain.model.Wind
import com.bogdan801.weatheraggregator.presentation.theme.Theme
import com.bogdan801.weatheraggregator.presentation.theme.WeatherAggregatorTheme

@Composable
fun ExpandableWeatherSlice(
    modifier: Modifier,
    isExpanded: Boolean = false,
    data: WeatherSlice? = null,
    displayTitles: Boolean = data == null
) {
    BoxWithConstraints(modifier = modifier.background(color = Color.Gray).clipToBounds()) {
        val columnHeight by animateDpAsState(
            targetValue = if(!isExpanded) (maxHeight / 4f) * 7f else maxHeight,
            animationSpec = tween(200)
        )
        Log.d("puk", (columnHeight-maxHeight).toString())
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .requiredHeight(columnHeight)
                .offset(y = (columnHeight-maxHeight)/2)
                .fillMaxWidth()
                .padding(start = if (displayTitles) 8.dp else 0.dp),
            horizontalAlignment = if(displayTitles) Alignment.Start else Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {

            if(displayTitles) {
                Text(text = "Time")
                Text(text = "Cloudiness")
                Text(text = "Temperature")
                Text(text = "Precipitation, %")
                Text(text = "Pressure, mm")
                Text(text = "Humidity, %")
                Text(text = "Wind, m/s")
            }
            else {

            }
        }
    }
}

@Preview
@Composable
fun PrevSlice() {
    val slice = WeatherSlice(
        time = "12:00",
        temperature = -10,
        skyCondition = SkyCondition("4_s_5_d"),
        precipitationProbability = 99,
        pressure = 755,
        humidity = 80,
        wind = Wind.create(5, 2)
    )

    WeatherAggregatorTheme(Theme.Light) {
        var isExpanded by remember {
            mutableStateOf(false)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            ExpandableWeatherSlice(
                modifier = Modifier.size(200.dp, 500.dp),
                //data = slice,
                isExpanded = isExpanded
            )
            Button(onClick = { isExpanded = !isExpanded }) {
                Text(text = if (isExpanded) "Expanded" else "Unexpanded")
            }
        }
    }
}