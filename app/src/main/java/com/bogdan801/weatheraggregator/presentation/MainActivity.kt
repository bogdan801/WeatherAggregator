package com.bogdan801.weatheraggregator.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.navigation.compose.rememberNavController
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

        setContent {
            WeatherAggregatorTheme(theme.value) {
                //setting up system bars color
                val statusBar = when(theme.value){
                    Theme.Auto -> if(isSystemInDarkTheme()) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.secondary
                    Theme.Light -> MaterialTheme.colors.secondary
                    Theme.Dark -> MaterialTheme.colors.primaryVariant
                }
                val systemUiController = rememberSystemUiController()
                systemUiController.setNavigationBarColor(MaterialTheme.colors.secondary)
                systemUiController.setStatusBarColor(statusBar)

                Surface(modifier = Modifier.fillMaxSize()){
                    Navigation(navController = rememberNavController())
                }
            }
        }
    }
}
