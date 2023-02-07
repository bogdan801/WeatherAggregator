package com.bogdan801.weatheraggregator.presentation.composables.repeatable

import android.util.Log
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bogdan801.weatheraggregator.data.util.toFormattedString
import com.bogdan801.weatheraggregator.presentation.screens.home.WeatherDataState

@Composable
fun DataSourceCard(
    modifier: Modifier = Modifier,
    dataState: WeatherDataState,
    isSelected: Boolean = false,
    onLongPress: (Boolean) -> Unit = {},
    onTap: (Boolean) -> Unit ={}
) {
    val density = LocalDensity.current
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
                    Row(modifier = Modifier.fillMaxSize()) {
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


                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val width = maxWidth
                            val sliceWidth = 70.dp
                            val lazyListState = rememberLazyListState()

                            val firstVisibleIndex by remember {
                                derivedStateOf {
                                    val visibleList = lazyListState.layoutInfo.visibleItemsInfo
                                    if(visibleList.isNotEmpty()) visibleList[0].index
                                    else null
                                }
                            }
                            val lastVisibleIndex by remember {
                                derivedStateOf {
                                    val visibleList = lazyListState.layoutInfo.visibleItemsInfo
                                    if(visibleList.isNotEmpty()) visibleList.last().index
                                    else null
                                }
                            }
                            val firstVisibleOffset by remember {
                                derivedStateOf {
                                    with(density) {
                                        lazyListState.firstVisibleItemScrollOffset.toDp()
                                    }
                                }
                            }
                            val lastVisibleOffset by remember {
                                derivedStateOf {
                                    if((firstVisibleIndex != null) && (lastVisibleIndex != null)){
                                        var sumWidth = 0.dp
                                        (firstVisibleIndex!!..lastVisibleIndex!!).forEach { id ->
                                            sumWidth += sliceWidth * dataState.data.weatherByDates[id].weatherByHours.size
                                        }

                                        val result = sumWidth - (firstVisibleOffset + width)

                                        return@derivedStateOf if(result >= 0.dp) result else 0.dp
                                    }
                                    else 0.dp
                                }
                            }

                            LazyRow(
                                modifier = Modifier
                                    .fillMaxSize(),
                                state = lazyListState
                            ){
                                itemsIndexed(dataState.data.weatherByDates) { index, day ->
                                    val itemWidth = sliceWidth * day.weatherByHours.size

                                    val startOffset =
                                        if((firstVisibleIndex != null) && (firstVisibleIndex == index)) firstVisibleOffset
                                        else 0.dp

                                    val lastOffset =
                                        if((lastVisibleIndex != null) && (lastVisibleIndex == index)) lastVisibleOffset
                                        else 0.dp

                                    Column(modifier = Modifier.fillMaxHeight()) {
                                        Box(
                                            modifier = Modifier
                                                .width(itemWidth)
                                                .height(dayLineHeight)
                                                .padding(
                                                    start = startOffset,
                                                    end = lastOffset + 0.5.dp
                                                )
                                                .background(MaterialTheme.colors.surface),
                                            contentAlignment = Alignment.Center
                                        ){
                                            Text(
                                                text = day.date.toFormattedString(),
                                                color = MaterialTheme.colors.onSurface.copy(0.8f),
                                                style = MaterialTheme.typography.caption,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Row(modifier = Modifier.fillMaxHeight()) {
                                            day.weatherByHours.forEachIndexed { index, weatherSlice ->
                                                ExpandableWeatherSlice(
                                                    modifier = Modifier
                                                        .width(sliceWidth)
                                                        .fillMaxHeight()
                                                        .background(
                                                            if (index % 2 == 0) MaterialTheme.colors.surface.copy(0.42f)
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
    }
}