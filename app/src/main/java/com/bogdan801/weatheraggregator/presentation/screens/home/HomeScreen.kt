package com.bogdan801.weatheraggregator.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.drawToBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.bogdan801.weatheraggregator.R
import com.bogdan801.weatheraggregator.presentation.theme.Theme
import com.bogdan801.weatheraggregator.presentation.theme.WeatherAggregatorTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@OptIn(ExperimentalPagerApi::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController? = null,
    viewModel: HomeScreenViewModel = hiltViewModel()
){
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    val scaffoldState = rememberScaffoldState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        snackbarHost = {scaffoldState.snackbarHostState}
    ) {
        //states for animated theme transition
        val imageState = remember { mutableStateOf<ImageBitmap?>(null) }
        val iconCoordinates = remember { mutableStateOf(Offset.Zero) }
        val isAnimating = remember { mutableStateOf(false)}
        val animatable = remember { Animatable(50f) }
        
        Box(modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colors.secondary,
                        if ((viewModel.themeState.value == Theme.Dark) ||
                            (viewModel.themeState.value == Theme.Auto && isSystemInDarkTheme())
                        ) MaterialTheme.colors.secondary
                        else MaterialTheme.colors.primaryVariant
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
        ) {
            //current tab state
            val pageState = rememberPagerState()
            Column(modifier = Modifier.fillMaxSize()) {
                //top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        modifier = Modifier.padding(8.dp),
                        onClick = {},
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.secondary,
                            contentColor = MaterialTheme.colors.onSurface
                        ),
                        elevation = null,
                        border = BorderStroke(width = 1.dp, color = Color.White.copy(alpha = 0.5f))
                    ) {
                        Row {
                            Icon(painter = painterResource(id = R.drawable.ic_location), contentDescription = "location")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Change location", style = MaterialTheme.typography.h4)
                        }
                    }

                    var count by remember { mutableStateOf(viewModel.themeState.value.ordinal) }
                    var isEnabled by remember { mutableStateOf(true) }
                    val isDarkTheme = isSystemInDarkTheme()
                    IconButton(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .onGloballyPositioned { coordinates ->
                                iconCoordinates.value = coordinates.boundsInRoot().center
                            },
                        enabled = isEnabled,
                        onClick = {
                            count++
                            val currentOrdinal = count % 3
                            viewModel.setTheme(currentOrdinal, context)

                            //animated transition between themes
                            if((currentOrdinal == 2) || (currentOrdinal == 1 && isDarkTheme) || (currentOrdinal == 0 && !isDarkTheme)){
                                coroutineScope.launch {
                                    isEnabled = false

                                    //save screenshot
                                    imageState.value = view.drawToBitmap().asImageBitmap()

                                    val diagonal = sqrt((view.width * view.width + view.height * view.height).toFloat())
                                    isAnimating.value = true
                                    animatable.animateTo(
                                        diagonal,
                                        tween(350)
                                    )
                                    isAnimating.value = false

                                    animatable.snapTo(50f)
                                    isEnabled = true
                                }
                            }
                        },
                    ) {
                        Icon(
                            painter = painterResource(
                                id = when(viewModel.themeState.value){
                                    Theme.Auto -> R.drawable.ic_auto
                                    Theme.Light -> R.drawable.ic_light_mode
                                    Theme.Dark -> R.drawable.ic_dark_mode
                                }
                            ),
                            contentDescription = "",
                            tint = MaterialTheme.colors.onSurface
                        )
                    }
                }

                HorizontalPager(
                    count = 2,
                    modifier = Modifier.fillMaxSize(),
                    state = pageState
                ) { index ->
                    when(index){
                        0 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 104.dp),
                                contentAlignment = Alignment.Center
                            ){
                                Text(text = "1")
                            }
                        }
                        1 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 104.dp),
                                contentAlignment = Alignment.Center
                            ){
                                Text(text = "2")
                            }
                        }
                    }
                }
            }

            //bottom bar
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .size(280.dp, 80.dp)
                    .align(Alignment.BottomCenter)
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
        
        //canvas that displays an animated theme transition
        if (isAnimating.value){
            Box(modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.99f)
                .drawBehind {
                    if (imageState.value != null) {
                        drawImage(imageState.value!!)
                        drawCircle(
                            color = Color.Black,
                            radius = animatable.value,
                            center = iconCoordinates.value,
                            blendMode = BlendMode.Xor
                        )
                    }
                }
            )
        }
    }
}

@Composable
@Preview
fun PreviewMain() {
    WeatherAggregatorTheme(Theme.Light) {

    }
}