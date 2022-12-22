package com.bogdan801.weatheraggregator.presentation.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.bogdan801.weatheraggregator.R
import com.bogdan801.weatheraggregator.presentation.theme.Theme

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeScreenViewModel = hiltViewModel()
){
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            var count by remember { mutableStateOf(viewModel.themeState.value.ordinal) }
            IconButton(
                onClick = {
                    count++
                    viewModel.setTheme(count % 3, context)
                },
            ) {
                Icon(
                    painter = painterResource(
                        id = when(viewModel.themeState.value){
                            Theme.Auto -> R.drawable.ic_light_mode
                            Theme.Light -> R.drawable.ic_dark_mode
                            Theme.Dark -> R.drawable.ic_auto
                        }
                    ),
                    contentDescription = "",
                    tint = MaterialTheme.colors.onSurface
                )
            }

        }
    }
}