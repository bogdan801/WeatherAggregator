package com.bogdan801.weatheraggregator.presentation.screens.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.core.view.drawToBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.bogdan801.weatheraggregator.R
import com.bogdan801.weatheraggregator.data.util.toFormattedDate
import com.bogdan801.weatheraggregator.presentation.composables.*
import com.bogdan801.weatheraggregator.presentation.theme.Theme
import com.bogdan801.weatheraggregator.presentation.theme.WeatherAggregatorTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalPagerApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController? = null,
    viewModel: HomeScreenViewModel = hiltViewModel()
){
    val context = LocalContext.current
    val view = LocalView.current
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val isDarkTheme = (viewModel.themeState.value == Theme.Dark) || (viewModel.themeState.value == Theme.Auto && isSystemInDarkTheme())
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

        BoxWithConstraints(modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        if (isDarkTheme) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.secondary,
                        if (isDarkTheme) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.primaryVariant
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
        ) {
            //current tab state
            val pageState = rememberPagerState()
            val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = viewModel.isLoadingState.value)
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = viewModel::updateWeatherData,
                indicator = { state, refreshTrigger ->
                    SwipeRefreshIndicator(
                        state = state,
                        refreshTriggerDistance = refreshTrigger,
                        backgroundColor = MaterialTheme.colors.secondaryVariant,
                        contentColor = MaterialTheme.colors.primary
                    )
                }
            ) {
                Column(modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .height(maxHeight)
                ) {
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
                        val isSystemInDarkTheme = isSystemInDarkTheme()
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
                                if((currentOrdinal == 2) || (currentOrdinal == 1 && isSystemInDarkTheme) || (currentOrdinal == 0 && !isSystemInDarkTheme)){
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
                                        Theme.Auto  -> R.drawable.ic_auto
                                        Theme.Light -> R.drawable.ic_light_mode
                                        Theme.Dark  -> R.drawable.ic_dark_mode
                                    }
                                ),
                                contentDescription = "",
                                tint = MaterialTheme.colors.onSurface
                            )
                        }
                    }

                    //pager
                    AdaptivePager(
                        count = 2,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                bottom = if (isPortrait) 104.dp else 0.dp,
                                end = if (!isPortrait) 104.dp else 0.dp
                            ),
                        state = pageState,
                        isHorizontal = isPortrait
                    ) { index ->
                        when(index){
                            0 -> {
                                //first page
                                AdaptiveDoubleLayout(
                                    modifier = Modifier.fillMaxSize(),
                                    firstPart = {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            Column(modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 24.dp)) {
                                                Text(
                                                    text = "Desnianske,\nUkraine",
                                                    style = MaterialTheme.typography.h2,
                                                    color = MaterialTheme.colors.onSurface
                                                )
                                                Text(
                                                    text = viewModel.selectedDay.date.toFormattedDate(context),
                                                    style = MaterialTheme.typography.h5,
                                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.65f)
                                                )
                                            }

                                            BoxWithConstraints(modifier = Modifier.fillMaxSize()){
                                                val width = maxWidth
                                                val height = maxHeight
                                                Row(
                                                    modifier = Modifier.fillMaxSize(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ){
                                                    val imagePadding = 8.dp
                                                    val maxSize = 300.dp
                                                    val sizeByWidth = min(width / 2 - (imagePadding * 2), maxSize)
                                                    val sizeByHeight = min(height - (imagePadding * 2), maxSize)
                                                    Spacer(modifier = Modifier.width(24.dp))
                                                    Image(
                                                        modifier = Modifier.size(min(sizeByWidth, sizeByHeight)),
                                                        painter = viewModel.selectedDay.skyCondition.getPainterResource(),
                                                        contentDescription = "Current condition"
                                                    )
                                                    Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                    ){
                                                        Row {
                                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                Text(
                                                                    text = viewModel.selectedDay.dayTemperature.toString(),
                                                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.9f),
                                                                    style = MaterialTheme.typography.h1
                                                                )
                                                                Text(
                                                                    modifier = Modifier.offset(y = (-20).dp),
                                                                    text = "Rainy",
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
                                    },
                                    secondPart = {
                                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                            val panelUnexpandedHeight = if(!isPortrait) maxHeight * 0.63f else maxHeight * 0.55f
                                            val panelExpandedHeight = if(!isPortrait) maxHeight else maxHeight * 0.9f
                                            val dayCardsHeight = if(!isPortrait) maxHeight * 0.28f else maxHeight * 0.35f
                                            val sourceSelectorHeight = maxHeight * 0.1f
                                            val width = maxWidth

                                            Column(modifier = Modifier
                                                .fillMaxWidth()
                                                .align(Alignment.BottomCenter)
                                            ) {
                                                LazyRow(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(dayCardsHeight)
                                                        .padding(4.dp)
                                                ) {
                                                    itemsIndexed(viewModel.data.weatherByDates){ index, dayCondition ->
                                                        DayCard(
                                                            modifier = Modifier
                                                                .padding(4.dp)
                                                                .fillMaxHeight()
                                                                .width((width - 40.dp) / 4f),
                                                            isSelected = viewModel.selectedDayState.value == index,
                                                            onCardClick = {
                                                                viewModel.setSelectedDay(index)
                                                            },
                                                            date = dayCondition.date,
                                                            skyCondition = dayCondition.skyCondition,
                                                            nightTemperature = dayCondition.nightTemperature,
                                                            dayTemperature = dayCondition.dayTemperature
                                                        )
                                                    }
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(sourceSelectorHeight)
                                                        //.background(Color.Gray)
                                                )
                                            }


                                            var isExpanded by remember { mutableStateOf(false) }
                                            val heightOfPanel by animateDpAsState(
                                                targetValue = if(isExpanded) panelExpandedHeight
                                                else panelUnexpandedHeight,
                                                animationSpec = tween(
                                                    durationMillis = 400
                                                )
                                            )
                                            DayWeatherPanel(
                                                modifier = Modifier
                                                    .align(Alignment.TopCenter)
                                                    .fillMaxWidth()
                                                    .height(heightOfPanel),
                                                data = viewModel.data.weatherByDates[viewModel.selectedDayState.value],
                                                isExpanded = isExpanded,
                                                onExpandClick = {
                                                    isExpanded = !isExpanded
                                                }
                                            )
                                        }
                                    },
                                    ratio = 4/5f,
                                    horizontalRatio = 1f
                                )
                            }
                            1 -> {
                                //second page
                                Box(modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 118.dp),
                                    contentAlignment = Alignment.Center
                                ){
                                    Text(text = "third", color = MaterialTheme.colors.onSurface)
                                }
                            }
                        }
                    }
                }
            }

            //trust levels panel
            val height = if(isPortrait) 211 else 118
            val yOffset = ((height+2) - (height * (pageState.currentPage+pageState.currentPageOffset))).dp
            Card (
                modifier = Modifier
                    .requiredWidth(maxWidth + 4.dp)
                    .height(height.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = yOffset)
                    .padding(end = if (!isPortrait) 104.dp else 0.dp),
                shape = RoundedCornerShape(
                    topStart = if(isPortrait) 20.dp else 0.dp,
                    topEnd = 20.dp
                ),
                backgroundColor = MaterialTheme.colors.secondary,
                border = BorderStroke(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f)
                )
            ){}

            //bottom bar
            BottomBar(
                modifier = Modifier.align(if(isPortrait) Alignment.BottomCenter else Alignment.CenterEnd),
                pageState = pageState,
                isHorizontal = isPortrait
            )
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