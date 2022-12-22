package com.bogdan801.weatheraggregator.presentation.util

import androidx.compose.ui.graphics.Color

fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}

/**
 * Helper function for interpolating between two colors
 * @param start start color
 * @param end end color
 * @param state state between 0 and 1
 * @return new color between [start] and [end] colors at [state]
 */
fun interpolateColor(start: Color, end: Color, state: Float): Color {
    return Color(
        lerp(start.red,   end.red,   state),
        lerp(start.green, end.green, state),
        lerp(start.blue,  end.blue,  state),
        lerp(start.alpha, end.alpha, state)
    )
}
