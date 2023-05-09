package com.bogdan801.weatheraggregator.presentation.composables

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.bogdan801.weatheraggregator.data.util.toFormattedDate
import com.bogdan801.weatheraggregator.domain.model.DayWeatherCondition
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.placeholder.placeholder

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WeatherOverview(
    modifier: Modifier = Modifier,
    locationName: String = "Desnianske,\nUkraine",
    selectedDay: DayWeatherCondition,
    isLoading: Boolean = false
) {
    val context = LocalContext.current
    AnimatedContent(
        targetState = selectedDay,
        modifier = modifier,
        transitionSpec = {
            fadeIn() with fadeOut()
        }
    ) { day ->
        Column(modifier = modifier) {
            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = locationName,
                style = MaterialTheme.typography.h2,
                color = MaterialTheme.colors.onSurface
            )
            Text(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .placeholder(
                        visible = isLoading,
                        color = MaterialTheme.colors.surface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp),
                        highlight = PlaceholderHighlight.shimmer()
                    ),
                text = day.date.toFormattedDate(context),
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.65f)
            )

            BoxWithConstraints(modifier = Modifier.fillMaxSize()){
                val width = maxWidth
                val height = maxHeight
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    val imagePadding = 8.dp
                    val maxSize = 150.dp
                    val sizeByWidth = min(width / 2 - (imagePadding * 2), maxSize)
                    val sizeByHeight = min(height - (imagePadding * 2), maxSize)
                    Spacer(modifier = Modifier.width(24.dp))
                    Image(
                        modifier = Modifier
                            .size(min(sizeByWidth, sizeByHeight))
                            .placeholder(
                                visible = isLoading,
                                color = MaterialTheme.colors.surface.copy(alpha = 0.5f),
                                shape = CircleShape,
                                highlight = PlaceholderHighlight.shimmer()
                            ),
                        painter = day.skyCondition.getPainterResource(),
                        contentDescription = "Current condition"
                    )
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Row(modifier = Modifier
                            .placeholder(
                                visible = isLoading,
                                color = MaterialTheme.colors.surface.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.medium,
                                highlight = PlaceholderHighlight.shimmer()
                            )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day.dayTemperature.toString(),
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.h1
                                )
                                Text(
                                    modifier = Modifier.offset(y = (-20).dp),
                                    text = day.skyCondition.textDescription,
                                    color = MaterialTheme.colors.onSurface,
                                    style = MaterialTheme.typography.h3
                                )
                            }
                            Text(
                                modifier = Modifier.offset(y = 10.dp),
                                text = "°C",
                                color = MaterialTheme.colors.onSurface,
                                style = MaterialTheme.typography.h5
                            )
                        }
                    }
                }
            }
        }
    }
}