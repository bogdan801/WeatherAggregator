package com.bogdan801.weatheraggregator.presentation.composables

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bogdan801.weatheraggregator.R
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalAnimationApi::class)
@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    pageState: PagerState,
    isHorizontal: Boolean = true
) {
    val coroutineScope = rememberCoroutineScope()

    if(isHorizontal){
        Box(
            modifier = modifier
                .padding(12.dp)
                .size(280.dp, 80.dp)
                .shadow(
                    elevation = 5.dp,
                    shape = MaterialTheme.shapes.large,
                    spotColor = Color.Black.copy(alpha = 0.3f)
                )
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colors.secondaryVariant)
        ) {
            Row(modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp, 60.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.1f - ((pageState.currentPageOffset + pageState.currentPage) / 10)))
                        .clickable {
                            coroutineScope.launch {
                                pageState.animateScrollToPage(0)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.weight(2f)){
                            AnimatedContent(
                                targetState = pageState.currentPage == 0,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 3.dp),
                                transitionSpec = {
                                    fadeIn() with fadeOut()
                                }
                            ) { isOnZeroPage ->
                                Icon(
                                    painter = painterResource(
                                        id = if(isOnZeroPage) R.drawable.ic_weather
                                        else R.drawable.ic_weather_unselected
                                    ),
                                    contentDescription = "Select Weather",
                                    tint = MaterialTheme.colors.primary
                                )
                            }
                        }
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "Weather",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(80.dp, 60.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colors.primary.copy(alpha = (pageState.currentPageOffset + pageState.currentPage) / 10))
                        .clickable {
                            coroutineScope.launch {
                                pageState.animateScrollToPage(1)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.weight(2f)){
                            AnimatedContent(
                                targetState = pageState.currentPage == 1,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 3.dp),
                                transitionSpec = {
                                    fadeIn() with fadeOut()
                                }
                            ) { isOnPageOne ->
                                Icon(
                                    painter = painterResource(
                                        id = if(isOnPageOne) R.drawable.ic_data_sources
                                        else R.drawable.ic_data_sources_unselected
                                    ),
                                    contentDescription = "Select Weather Source",
                                    tint = MaterialTheme.colors.primary
                                )
                            }
                        }
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "Data Sources",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
            }
        }
    }
    else {
        Box(
            modifier = modifier
                .padding(12.dp)
                .size(80.dp, 210.dp)
                .shadow(
                    elevation = 5.dp,
                    shape = MaterialTheme.shapes.large,
                    spotColor = Color.Black.copy(alpha = 0.3f)
                )
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colors.secondaryVariant)
        ) {
            Column(modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp, 60.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.1f - ((pageState.currentPageOffset + pageState.currentPage) / 10)))
                        .clickable {
                            coroutineScope.launch {
                                pageState.animateScrollToPage(0)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.weight(2f)){
                            AnimatedContent(
                                targetState = pageState.currentPage == 0,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 3.dp),
                                transitionSpec = {
                                    fadeIn() with fadeOut()
                                }
                            ) { isOnZeroPage ->
                                Icon(
                                    painter = painterResource(
                                        id = if(isOnZeroPage) R.drawable.ic_weather
                                        else R.drawable.ic_weather_unselected
                                    ),
                                    contentDescription = "Select Weather",
                                    tint = MaterialTheme.colors.primary
                                )
                            }
                        }
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "Weather",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(70.dp, 60.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colors.primary.copy(alpha = (pageState.currentPageOffset + pageState.currentPage) / 10))
                        .clickable {
                            coroutineScope.launch {
                                pageState.animateScrollToPage(1)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.weight(2f)){
                            AnimatedContent(
                                targetState = pageState.currentPage == 1,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 3.dp),
                                transitionSpec = {
                                    fadeIn() with fadeOut()
                                }
                            ) { isOnPageOne ->
                                Icon(
                                    painter = painterResource(
                                        id = if(isOnPageOne) R.drawable.ic_data_sources
                                        else R.drawable.ic_data_sources_unselected
                                    ),
                                    contentDescription = "Select Weather Source",
                                    tint = MaterialTheme.colors.primary
                                )
                            }
                        }
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "Data Sources",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
            }
        }
    }
}