package com.bogdan801.weatheraggregator.presentation.composables.repeatable

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bogdan801.weatheraggregator.R
import com.bogdan801.weatheraggregator.data.util.toDegree
import com.bogdan801.weatheraggregator.domain.model.WeatherSlice
import com.bogdan801.weatheraggregator.domain.model.Wind

@Composable
fun ExpandableWeatherSlice(
    modifier: Modifier,
    isExpanded: Boolean = false,
    data: WeatherSlice? = null,
    displayTitles: Boolean = data == null,
    displayDayTitle: Boolean = false,
    calculatedItemHeight: (Dp) -> Unit = {}
) {
    BoxWithConstraints(modifier = modifier.clipToBounds()) {
        val columnHeight by animateDpAsState(
            targetValue = if(!isExpanded) (maxHeight / 4f) * 7f else maxHeight,
            animationSpec = tween(200)
        )
        val height = maxHeight / 7
        calculatedItemHeight(height)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .requiredHeight(columnHeight)
                .offset(y = (columnHeight - maxHeight) / 2)
                .fillMaxWidth()
                .padding(start = if (displayTitles) 8.dp else 0.dp),
            horizontalAlignment = if(displayTitles) Alignment.Start else Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            if(displayTitles) {
                if (displayDayTitle){
                    Box(
                        modifier = Modifier.height(height),
                        contentAlignment = Alignment.CenterStart
                    ){
                        Text(
                            text = "Day",
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                Box(
                    modifier = Modifier.height(height),
                    contentAlignment = Alignment.CenterStart
                ){
                    Text(
                        text = "Time",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                    )
                }

                Box(
                    modifier = Modifier.height(height),
                    contentAlignment = Alignment.CenterStart
                ){
                    Text(text = "Cloudiness",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                    )
                }

                Box(
                    modifier = Modifier.height(height),
                    contentAlignment = Alignment.CenterStart
                ){
                    Text(text = "Temperature",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                    )
                }

                Box(
                    modifier = Modifier.height(height),
                    contentAlignment = Alignment.CenterStart
                ){
                    Text(text = "Precipitation, %",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                    )
                }

                Box(
                    modifier = Modifier.height(height),
                    contentAlignment = Alignment.CenterStart
                ){
                    Text(text = "Pressure, mm",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                    )
                }

                Box(
                    modifier = Modifier.height(height),
                    contentAlignment = Alignment.CenterStart
                ){
                    Text(text = "Humidity, %",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                    )
                }

                Box(
                    modifier = Modifier.height(height),
                    contentAlignment = Alignment.CenterStart
                ){
                    Text(text = "Wind, m/s",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
            else {
                Box(
                    modifier = Modifier.height(height),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text = data!!.time,
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                    )
                }

                Box(
                    modifier = Modifier.height(height),
                    contentAlignment = Alignment.Center
                ){
                    Image(modifier = Modifier.requiredSize(30.dp), painter = data!!.skyCondition.getPainterResource(), contentDescription = "Weather icon")
                }

                Box(
                    modifier = Modifier.height(height),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text = data!!.temperature.toDegree(),
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                    )
                }

                Box(
                    modifier = Modifier.height(height),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text = if(data!!.precipitationProbability == -1) "-" else data.precipitationProbability.toString(),
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                    )
                }

                Box(
                    modifier = Modifier.height(height),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text = data!!.pressure.toString(),
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                    )
                }

                Box(
                    modifier = Modifier.height(height),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text = data!!.humidity.toString(),
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                    )
                }

                Box(
                    modifier = Modifier.height(height),
                    contentAlignment = Alignment.Center
                ){
                    Row(verticalAlignment = Alignment.CenterVertically){
                        Icon(
                            modifier = Modifier
                                .width(8.dp)
                                .aspectRatio(3f / 2)
                                .rotate(
                                    when (data!!.wind) {
                                        is Wind.East -> 0f
                                        is Wind.SouthEast -> 45f
                                        is Wind.South -> 90f
                                        is Wind.SouthWest -> 135f
                                        is Wind.West -> 180f
                                        is Wind.NorthWest -> 225f
                                        is Wind.North -> 270f
                                        is Wind.NorthEast -> 315f
                                    }
                                ),
                            painter = painterResource(id = R.drawable.ic_arrow),
                            contentDescription = "wind",
                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                        )
                        Text(
                            modifier = Modifier.padding(vertical = 4.dp),
                            text = " " + when(data.wind){
                                is Wind.East      -> data.wind.power.toString()
                                is Wind.North     -> data.wind.power.toString()
                                is Wind.NorthEast -> data.wind.power.toString()
                                is Wind.NorthWest -> data.wind.power.toString()
                                is Wind.South     -> data.wind.power.toString()
                                is Wind.SouthEast -> data.wind.power.toString()
                                is Wind.SouthWest -> data.wind.power.toString()
                                is Wind.West      -> data.wind.power.toString()
                            },
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}