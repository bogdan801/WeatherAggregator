package com.bogdan801.weatheraggregator.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.bogdan801.weatheraggregator.domain.model.SkyCondition
import com.bogdan801.weatheraggregator.domain.model.WeatherSlice
import com.bogdan801.weatheraggregator.domain.model.Wind
import com.bogdan801.weatheraggregator.presentation.composables.ExpandableWeatherSlice
import com.bogdan801.weatheraggregator.presentation.navigation.Navigation
import com.bogdan801.weatheraggregator.presentation.theme.Theme
import com.bogdan801.weatheraggregator.presentation.theme.WeatherAggregatorTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var theme: MutableState<Theme>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val slice = WeatherSlice(
            time = "12:00",
            temperature = -10,
            skyCondition = SkyCondition("4_s_5_d"),
            precipitationProbability = 99,
            pressure = 755,
            humidity = 80,
            wind = Wind.create(5, 2)
        )

        setContent {
            WeatherAggregatorTheme(theme.value) {
                //setting up system bars color
                val statusBar = when(theme.value){
                    Theme.Auto -> if(isSystemInDarkTheme()) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.secondary
                    Theme.Light -> MaterialTheme.colors.secondary
                    Theme.Dark -> MaterialTheme.colors.primaryVariant
                }
                val systemUiController = rememberSystemUiController()
                systemUiController.setStatusBarColor(statusBar)
                systemUiController.setNavigationBarColor(MaterialTheme.colors.secondary)

                //screens and navigation
                //Navigation(navController = rememberNavController())

                var isExpanded by remember {
                    mutableStateOf(false)
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    ExpandableWeatherSlice(
                        modifier = Modifier.size(200.dp, 500.dp).align(Alignment.TopStart),
                        //data = slice,
                        isExpanded = isExpanded
                    )
                    Button(
                        modifier = Modifier.align(Alignment.TopEnd),
                        onClick = { isExpanded = !isExpanded }
                    ) {
                        Text(text = if (isExpanded) "Expanded" else "Unexpanded")
                    }
                }
            }
        }
    }
}
