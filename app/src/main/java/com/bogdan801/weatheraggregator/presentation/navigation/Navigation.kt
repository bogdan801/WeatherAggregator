package com.bogdan801.weatheraggregator.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bogdan801.weatheraggregator.presentation.screens.home.HomeScreen

@Composable
fun Navigation(
    navController: NavHostController
){
    NavHost(navController = navController, startDestination = Screen.HomeScreen.route){
        composable(Screen.HomeScreen.route){
            HomeScreen(navController = navController)
        }
    }
}