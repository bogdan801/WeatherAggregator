package com.bogdan801.weatheraggregator.presentation

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.bogdan801.weatheraggregator.domain.model.getOblastListFromFile
import com.bogdan801.weatheraggregator.domain.repository.Repository
import com.bogdan801.weatheraggregator.presentation.navigation.Navigation
import com.bogdan801.weatheraggregator.presentation.theme.Theme
import com.bogdan801.weatheraggregator.presentation.theme.WeatherAggregatorTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var theme: MutableState<Theme>

    @Inject
    lateinit var repo: Repository

    @OptIn(ExperimentalTime::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val time = measureTime{
                if(repo.getOblastList().isEmpty()){
                    val allLocations = getOblastListFromFile(this@MainActivity)
                    allLocations.forEach { oblast ->
                        oblast.listOfRegions.forEach { region ->
                            region.locations.forEach { location ->
                                repo.insertLocation(location)
                            }
                        }
                    }
                }
            }
            val gg = time.inWholeMilliseconds.toString()

            Toast.makeText(this@MainActivity, "Час на збереження локацій: ${gg}ms", Toast.LENGTH_LONG).show()
        }

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
