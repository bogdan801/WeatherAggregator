package com.bogdan801.weatheraggregator.presentation.composables

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bogdan801.weatheraggregator.R
import com.bogdan801.weatheraggregator.domain.model.DayWeatherCondition

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DayWeatherPanel(
    modifier: Modifier = Modifier,
    data: DayWeatherCondition,
    isExpanded: Boolean = false,
    onExpandClick: () -> Unit = {},
    slideRight: Boolean = true
) {
    val localDensity = LocalDensity.current
    var columnWidth by remember { mutableStateOf(0.dp) }
    Column(
        modifier = modifier.onGloballyPositioned { coordinates ->
            columnWidth = with(localDensity) { coordinates.size.width.toDp() }
        }
    ) {

        AnimatedContent(
            targetState = data,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = {
                        if(slideRight) -it
                        else it
                    }
                ) with slideOutHorizontally(
                    targetOffsetX = {
                        if(slideRight) it
                        else -it
                    }
                )
            }
        ) { newData ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                ExpandableWeatherSlice(
                    modifier = Modifier
                        .background(MaterialTheme.colors.secondary)
                        .fillMaxHeight()
                        .width(100.dp),
                    isExpanded = isExpanded,
                    displayTitles = true
                )
                LazyRow(modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                ){
                    itemsIndexed(newData.weatherByHours){ index, slice ->
                        val sliceWidth =
                            if((60.dp * newData.weatherByHours.size) < (columnWidth-100.dp))
                                (columnWidth-100.dp) / newData.weatherByHours.size
                            else 60.dp
                        ExpandableWeatherSlice(
                            modifier = modifier
                                .fillMaxHeight()
                                .width(sliceWidth)
                                .background(
                                    if (index % 2 == 0) MaterialTheme.colors.onBackground
                                    else MaterialTheme.colors.secondary
                                ),
                            isExpanded = isExpanded,
                            data = slice
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(MaterialTheme.colors.onBackground)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onExpandClick
                ),
            contentAlignment = Alignment.Center
        ){
            Row(verticalAlignment = Alignment.CenterVertically) {
                val iconRotation by animateFloatAsState(
                    targetValue = if(!isExpanded) 0f else 180f,
                    animationSpec = tween(200)
                )

                AnimatedContent(
                    targetState = isExpanded,
                    transitionSpec = {
                        fadeIn() with fadeOut()
                    }
                ) { isTextForExpanded ->
                    Text(
                        text = if(isTextForExpanded) "Less Details" else "More Details",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.65f)
                    )
                }

                Icon(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(13.dp, 6.dp)
                        .graphicsLayer {
                            rotationX = iconRotation
                        },
                    painter = painterResource(id = R.drawable.ic_expand),
                    contentDescription = "Expand arrow",
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.65f)
                )
            }
        }
    }
}