package com.bogdan801.weatheraggregator.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bogdan801.weatheraggregator.domain.model.*
import com.bogdan801.weatheraggregator.presentation.theme.WeatherAggregatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val skyCondition = SkyCondition(
            Cloudiness.CloudyWithClearing,
            Precipitation.Snow(SnowLevel.Five),
            TimeOfDay.Night
        )

        setContent {
            WeatherAggregatorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Image(
                            modifier = Modifier
                                .size(150.dp),
                            painter = skyCondition.getPainterResource(),
                            contentDescription = "Picture"
                        )
                    }
                }
            }
        }
    }
}
