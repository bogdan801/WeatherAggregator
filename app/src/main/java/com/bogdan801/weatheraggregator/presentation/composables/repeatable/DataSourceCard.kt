package com.bogdan801.weatheraggregator.presentation.composables.repeatable

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bogdan801.weatheraggregator.R
import com.bogdan801.weatheraggregator.data.util.toFormattedString
import com.bogdan801.weatheraggregator.domain.model.WeatherData
import com.bogdan801.weatheraggregator.domain.model.WeatherSourceDomain
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
    var cardHeightState by remember {
        mutableStateOf(0.dp)
    }

    Box(modifier = modifier){
        when(dataState){
            is WeatherDataState.Data -> {
                var isExpanded by remember { mutableStateOf(false) }
                val cardHeight by animateDpAsState(
                    targetValue =
                    if(isExpanded) 300.dp
                    else 72.dp,
                    animationSpec = tween(200)
                )
                cardHeightState = cardHeight
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
                            DataSourceCardHeader(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp),
                                data = dataState.data,
                                isLinkBrowsable = (dataState.d.domain != WeatherSourceDomain.OpenWeather) && (!isSelected)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(64.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            if (!isSelected) isExpanded = !isExpanded
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
                                        itemsIndexed(dataState.data.weatherByDates) { dayIndex, day ->
                                            val itemWidth = sliceWidth * day.weatherByHours.size

                                            val startOffset =
                                                if((firstVisibleIndex != null) && (firstVisibleIndex == dayIndex)) firstVisibleOffset
                                                else 0.dp

                                            val lastOffset =
                                                if((lastVisibleIndex != null) && (lastVisibleIndex == dayIndex)) lastVisibleOffset
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
                                                    day.weatherByHours.forEachIndexed { sliceIndex, weatherSlice ->
                                                        ExpandableWeatherSlice(
                                                            modifier = Modifier
                                                                .width(sliceWidth)
                                                                .fillMaxHeight()
                                                                .background(
                                                                    if (isSliceEven(
                                                                            dayIndex,
                                                                            sliceIndex,
                                                                            dataState.data
                                                                        )
                                                                    )
                                                                        MaterialTheme.colors.surface.copy(
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
            }
            is WeatherDataState.IsLoading -> {
                cardHeightState = 72.dp
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    backgroundColor = MaterialTheme.colors.secondary ,
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
                            DataSourceCardHeader(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp),
                                data = dataState.data,
                                isLinkBrowsable = (dataState.d.domain != WeatherSourceDomain.OpenWeather) && (!isSelected)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(64.dp),
                                contentAlignment = Alignment.Center
                            ){
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 3.dp
                                )
                            }
                        }
                    }
                }
            }
            is WeatherDataState.Error -> {
                cardHeightState = 106.dp
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(106.dp),
                    backgroundColor = MaterialTheme.colors.onPrimary,
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
                            DataSourceCardHeader(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp),
                                data = dataState.data,
                                isLinkBrowsable = (dataState.d.domain != WeatherSourceDomain.OpenWeather) && (!isSelected)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(64.dp),
                                contentAlignment = Alignment.Center
                            ){
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(id = R.drawable.ic_error),
                                    contentDescription = "Error icon",
                                    tint = MaterialTheme.colors.error
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .padding(horizontal = 8.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colors.onSurface.copy(0.1f))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ){
                            Text(
                                text = dataState.error!!,
                                color = MaterialTheme.colors.onSurface.copy(0.65f),
                                style = MaterialTheme.typography.h5
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeightState),
                backgroundColor = MaterialTheme.colors.secondary.copy(0.5f),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(width = 3.dp, color = MaterialTheme.colors.primary),
                elevation = 0.dp
            ){
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Icon(
                        modifier = Modifier.size(46.dp),
                        painter = painterResource(id = R.drawable.ic_check_mark),
                        contentDescription = "Check mark",
                        tint = MaterialTheme.colors.primary
                    )
                }
            }
        }
    }
}

@Composable
fun DataSourceCardHeader(
    modifier: Modifier = Modifier,
    data: WeatherData,
    isLinkBrowsable: Boolean = true
){
    val context = LocalContext.current
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = data.domain.name,
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.overline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                modifier = Modifier.size(16.dp),
                painter = data.getDomainPainter(),
                contentDescription = ""
            )
        }
        Text(
            modifier = Modifier.clickable {
                if(isLinkBrowsable){
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(data.url))
                    context.startActivity(browserIntent)
                }
            },
            text = data.url,
            color = MaterialTheme.colors.onSurface.copy(0.65f),
            style = MaterialTheme.typography.caption,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun isSliceEven(dayIndex: Int, sliceIndex: Int, data: WeatherData): Boolean {
    var sum = 0
    (0 until dayIndex).forEach { index ->
        sum += data.weatherByDates[index].weatherByHours.size
    }
    sum += sliceIndex
    return sum % 2 == 0
}