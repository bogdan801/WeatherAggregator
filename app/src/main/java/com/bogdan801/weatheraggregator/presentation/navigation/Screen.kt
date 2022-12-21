package com.bogdan801.weatheraggregator.presentation.navigation

sealed class Screen(val route: String){
    object HomeScreen: Screen("home")
}
