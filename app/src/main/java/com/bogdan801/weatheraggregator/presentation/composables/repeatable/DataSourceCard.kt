package com.bogdan801.weatheraggregator.presentation.composables.repeatable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.bogdan801.weatheraggregator.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bogdan801.weatheraggregator.presentation.screens.home.WeatherDataState

@Composable
fun DataSourceCard(
    modifier: Modifier = Modifier,
    dataState: WeatherDataState,
    isSelected: Boolean = false,
    onLongPress: (Boolean) -> Unit = {},
    onTap: (Boolean) -> Unit ={}
) {
    var isExpanded by remember { mutableStateOf(false) }
    Box(modifier = modifier){
        val cardHeight by animateDpAsState(
            targetValue =
            if(isExpanded) 300.dp
            else 72.dp,
            animationSpec = tween(200)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight),
            backgroundColor = MaterialTheme.colors.secondary,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, Color.White.copy(0.2f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(MaterialTheme.colors.onPrimary),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = dataState.data.domain.name,
                                color = MaterialTheme.colors.onSurface,
                                style = MaterialTheme.typography.overline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Image(
                                modifier = Modifier.size(16.dp),
                                painter = dataState.data.getDomainPainter(),
                                contentDescription = ""
                            )
                        }
                        Text(
                            text = dataState.data.url,
                            color = MaterialTheme.colors.onSurface.copy(0.65f),
                            style = MaterialTheme.typography.caption,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(64.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    isExpanded = !isExpanded
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ){
                        val xRotation by animateFloatAsState(
                            targetValue = if(!isExpanded) 0f else 180f,
                            animationSpec = tween(200)
                        )
                        Icon(
                            modifier = Modifier
                                .size(18.dp)
                                .graphicsLayer {
                                    rotationX = xRotation
                                },
                            painter = painterResource(id = R.drawable.ic_expand),
                            contentDescription = "Icon",
                            tint = MaterialTheme.colors.primary
                        )
                    }
                }


                AnimatedVisibility(
                    visible = isExpanded,
                    enter = EnterTransition.None
                ) {
                    Row(modifier = Modifier
                        .fillMaxSize()
                    ) {
                        var dayLineHeight by remember { mutableStateOf(0.dp) }
                        ExpandableWeatherSlice(
                            modifier = Modifier
                                .width(100.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colors.onPrimary)
                                .padding(bottom = 4.dp),
                            isExpanded = true,
                            displayTitles = true,
                            displayDayTitle = true,
                            calculatedItemHeight = { height ->
                                dayLineHeight = height
                            }
                        )
                        
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                        ) {
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .height(dayLineHeight)
                                .background(MaterialTheme.colors.surface)
                            )
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ){
                                dataState.data.weatherByDates[0].weatherByHours.forEachIndexed { index, weatherSlice ->
                                    item {
                                        ExpandableWeatherSlice(
                                            modifier = Modifier
                                                .width(70.dp)
                                                .fillMaxHeight()
                                                .background(
                                                    if (index % 2 == 0) MaterialTheme.colors.surface.copy(
                                                        0.42f
                                                    )
                                                    else MaterialTheme.colors.onPrimary
                                                )
                                                .padding(bottom = 4.dp),
                                            isExpanded = true,
                                            data = weatherSlice
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}