package com.bogdan801.weatheraggregator.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.bogdan801.weatheraggregator.domain.repository.Repository
import com.bogdan801.weatheraggregator.presentation.navigation.Navigation
import com.bogdan801.weatheraggregator.presentation.theme.Theme
import com.bogdan801.weatheraggregator.presentation.theme.WeatherAggregatorTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var theme: MutableState<Theme>

    @Inject
    lateinit var repo: Repository

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
                systemUiController.setStatusBarColor(statusBar)
                systemUiController.setNavigationBarColor(MaterialTheme.colors.secondary)

                //screens and navigation
                Navigation(navController = rememberNavController())
            }
        }
    }
}
