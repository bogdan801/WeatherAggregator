package com.bogdan801.weatheraggregator.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class Theme {
    Auto,
    Light,
    Dark
}

private val LightColorPalette = lightColors(
    primary = thirdLight,
    primaryVariant = secondLight,
    secondary = firstLight,
    secondaryVariant = fourthLight,
    onSurface = fifthLight,
    surface = sixthLight,
    onPrimary = seventhLight,
    onSecondary = eighthLight,
    error = ninth,
    background = Color.White,
    onBackground = Color(0xFFDDE6F1)
)

private val DarkColorPalette = darkColors(
    primary = thirdDark,
    primaryVariant = secondDark,
    secondary = firstDark,
    secondaryVariant = fourthDark,
    onSurface = fifthDark,
    surface = sixthDark,
    onPrimary = seventhDark,
    onSecondary = eighthDark,
    error = ninth,
    background = Color.Black,
    onBackground = Color(0xFF252525)
)

@Composable
fun WeatherAggregatorTheme(theme: Theme = Theme.Auto, content: @Composable () -> Unit) {
    val colors = when (theme) {
        Theme.Light -> LightColorPalette
        Theme.Dark -> DarkColorPalette
        Theme.Auto -> {
            if(isSystemInDarkTheme()){
                DarkColorPalette
            }
            else{
                LightColorPalette
            }
        }
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}