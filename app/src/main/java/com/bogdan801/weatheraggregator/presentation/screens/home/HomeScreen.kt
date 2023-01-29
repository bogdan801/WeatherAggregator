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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.drawToBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.bogdan801.weatheraggregator.presentation.composables.*
import com.bogdan801.weatheraggregator.presentation.composables.layout.AdaptiveDoubleLayout
import com.bogdan801.weatheraggregator.presentation.composables.layout.AdaptivePager
import com.bogdan801.weatheraggregator.presentation.composables.repeatable.DayCard
import com.bogdan801.weatheraggregator.presentation.theme.Theme
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
                    var count by remember { mutableStateOf(viewModel.themeState.value.ordinal) }
                    var isEnabled by remember { mutableStateOf(true) }
                    val isSystemInDarkTheme = isSystemInDarkTheme()
                    TopAppBar(
                        modifier = Modifier.fillMaxWidth(),
                        themeState = viewModel.themeState,
                        onChangeLocationClick = {},
                        getIconCoordinates = { coordinates ->
                            iconCoordinates.value = coordinates
                        },
                        onChangeThemeClick = {
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
                        isEnabled = isEnabled
                    )

                    //pager
                    AdaptivePager(
                        count = 2,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                bottom = if (isPortrait) 100.dp else 0.dp,
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
                                        WeatherOverview(
                                            modifier = Modifier.fillMaxSize(),
                                            selectedDay = viewModel.selectedDay
                                        )
                                    },
                                    secondPart = {
                                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                            val columnWidth = maxWidth
                                            val dataSelectorHeight = 50.dp
                                            val heightRelation = if(isPortrait) 1.05f else 0.95f
                                            val dayCardsHeight = ((maxWidth - 40.dp) / 4f) * heightRelation
                                            val columnHeight = dayCardsHeight + dataSelectorHeight + 16.dp
                                            var slideRight by remember { mutableStateOf(true) }
                                            Column(modifier = Modifier
                                                .fillMaxWidth()
                                                .align(Alignment.BottomCenter)
                                            ) {
                                                LazyRow(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(4.dp)
                                                ) {
                                                    itemsIndexed(viewModel.currentData.weatherByDates){ index, dayCondition ->
                                                        DayCard(
                                                            modifier = Modifier
                                                                .padding(4.dp)
                                                                .width((columnWidth - 40.dp) / 4f)
                                                                .height(dayCardsHeight),
                                                            isSelected = viewModel.selectedDayState.value == index,
                                                            onCardClick = {
                                                                viewModel.setSelectedDay(index) { shouldSlideRight ->
                                                                    slideRight = shouldSlideRight
                                                                }
                                                            },
                                                            date = dayCondition.date,
                                                            skyCondition = dayCondition.skyCondition,
                                                            nightTemperature = dayCondition.nightTemperature,
                                                            dayTemperature = dayCondition.dayTemperature
                                                        )
                                                    }
                                                }
                                                DataSelector(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(dataSelectorHeight)
                                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                                    dataStateList = viewModel.dataListState.value,
                                                    selectedIndex = viewModel.selectedDataIndexState.value,
                                                    onDataSelected = { index, isError ->
                                                        if(isError){
                                                            coroutineScope.launch {
                                                                pageState.animateScrollToPage(1)
                                                            }
                                                        }
                                                        else {
                                                            viewModel.setSelectedData(index)
                                                        }
                                                    }
                                                )
                                            }

                                            var isExpanded by remember { mutableStateOf(false) }
                                            val heightOfPanel by animateDpAsState(
                                                targetValue =
                                                    if(isExpanded) maxHeight
                                                    else maxHeight - columnHeight,
                                                animationSpec = tween(
                                                    durationMillis = 400
                                                )
                                            )
                                            DayWeatherPanel(
                                                modifier = Modifier
                                                    .align(Alignment.TopCenter)
                                                    .fillMaxWidth()
                                                    .height(heightOfPanel),
                                                data = viewModel.selectedDay,
                                                isExpanded = isExpanded,
                                                onExpandClick = {
                                                    isExpanded = !isExpanded
                                                },
                                                slideRight = slideRight
                                            )
                                        }
                                    },
                                    ratio = 4/5.5f,
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
                                    Text(
                                        text = viewModel.trustLevels.value.toString(),
                                        color = MaterialTheme.colors.onSurface,
                                        style = MaterialTheme.typography.caption
                                    )
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
            ){
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier
                            .padding(
                                start = 16.dp,
                                top = 8.dp,
                                bottom = 4.dp
                            ),
                        text = "Trust levels",
                        style = MaterialTheme.typography.overline,
                        color = MaterialTheme.colors.onSurface
                    )
                    TrustLevelsSelector(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .padding(vertical = 0.dp, horizontal = 8.dp),
                        dataStateList = viewModel.dataListState.value,
                        levels = viewModel.trustLevels.value,
                        onLevelChanged = { newLevels ->
                            viewModel.setTrustLevels(newLevels)
                        }
                    )
                }
            }

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