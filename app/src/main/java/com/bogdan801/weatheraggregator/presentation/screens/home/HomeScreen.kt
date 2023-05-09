package com.bogdan801.weatheraggregator.presentation.screens.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.bogdan801.weatheraggregator.presentation.composables.repeatable.DataSourceCard
import com.bogdan801.weatheraggregator.presentation.composables.repeatable.DayCard
import com.bogdan801.weatheraggregator.presentation.theme.Theme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController? = null,
    viewModel: HomeScreenViewModel = hiltViewModel()
){
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val isDarkTheme = (viewModel.themeState.value == Theme.Dark) || (viewModel.themeState.value == Theme.Auto && isSystemInDarkTheme())
    val coroutineScope = rememberCoroutineScope()
    val pageState = rememberPagerState()


    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    ModalBottomSheetLayout(
        modifier = Modifier.fillMaxSize(),
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        scrimColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
        sheetContent = {
            LaunchedEffect(key1 = sheetState.isVisible) {
                if(!sheetState.isVisible) viewModel.openSelectLocationSheet(true)
            }

            if(viewModel.showSelectLocationSheet.value) {
                SelectLocationSheet(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                        .background(MaterialTheme.colors.onPrimary),
                    sheetState = sheetState,
                    onLocationSelected = { selectedLocation ->
                        viewModel.setTemporaryLocation(selectedLocation)
                        viewModel.setBlockBackPressOnLocationsSelection(false)
                        viewModel.openSelectLocationSheet(false)
                    }
                )
            }
            else {
                if(!viewModel.blockBackPressOnLocationsSelection.value){
                    BackHandler(enabled = true) {
                        viewModel.openSelectLocationSheet(true)
                    }
                }
                SelectDataSourcesSheet(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(MaterialTheme.colors.onPrimary),
                    location = viewModel.tempLocation.value,
                    onSourcesSelected = { selectedDomains ->
                        scope.launch {
                            sheetState.hide()
                            pageState.animateScrollToPage(0)
                        }
                        viewModel.openSelectLocationSheet(true)
                        viewModel.setSelectedData(0)
                        viewModel.setSelectedDay(0)
                        viewModel.setupDataFlows(viewModel.tempLocation.value, selectedDomains, true)
                        viewModel.setBlockBackPressOnLocationsSelection(true)
                    },
                    selectedDomains = viewModel.dataListState.map { it.data.domain }
                )
            }
        }
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
            val pageFraction by remember {
                derivedStateOf {
                    pageState.currentPageOffset + pageState.currentPage.toFloat()
                }
            }

            val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = viewModel.isRefreshingState.value)
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = viewModel::refreshAllWeatherData,
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
                        onChangeLocationClick = {
                            viewModel.openSelectLocationSheet(true)
                            scope.launch {
                                if(!sheetState.isVisible) sheetState.show()
                            }
                        },
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
                                        tween(durationMillis =  700, easing = FastOutSlowInEasing)
                                    )
                                    isAnimating.value = false

                                    animatable.snapTo(50f)
                                    isEnabled = true
                                }
                            }
                        },
                        isEnabled = isEnabled
                    )

                    if(viewModel.selectedLocation.value.isNotEmpty()){
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
                                        placeholderText = if(viewModel.dataListState.isEmpty()) "Please select data sources" else "",
                                        firstPart = {
                                            WeatherOverview(
                                                modifier = Modifier.fillMaxSize(),
                                                selectedDay = viewModel.selectedDay,
                                                locationName = viewModel.selectedLocation.value.name + ",\nUkraine",
                                                isLoading = viewModel.currentDataState.isLoading
                                            )
                                        },
                                        secondPart = {
                                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                                val columnWidth = maxWidth
                                                val dataSelectorHeight = 50.dp
                                                val heightRelation = if(isPortrait) 1.05f else 0.95f
                                                val dayCardsHeight = ((maxWidth - 40.dp) / 4f) * heightRelation
                                                val columnHeight = (if(viewModel.dataListState.size != 1) dataSelectorHeight else 0.dp) + dayCardsHeight + 16.dp
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
                                                        itemsIndexed(viewModel.currentDataState.data.weatherByDates){ index, dayCondition ->
                                                            DayCard(
                                                                modifier = Modifier
                                                                    .padding(4.dp)
                                                                    .width((columnWidth - 40.dp) / 4f)
                                                                    .height(dayCardsHeight),
                                                                isSelected = viewModel.selectedDayState.value == index,
                                                                isLoading = viewModel.currentDataState.isLoading,
                                                                onCardClick = {
                                                                    if(!viewModel.currentDataState.isLoading){
                                                                        viewModel.setSelectedDay(index) { shouldSlideRight ->
                                                                            slideRight = shouldSlideRight
                                                                        }
                                                                    }
                                                                },
                                                                date = dayCondition.date,
                                                                skyCondition = dayCondition.skyCondition,
                                                                nightTemperature = dayCondition.nightTemperature,
                                                                dayTemperature = dayCondition.dayTemperature
                                                            )
                                                        }
                                                    }
                                                    if(viewModel.dataListState.size > 1){
                                                        DataSelector(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(dataSelectorHeight)
                                                                .padding(
                                                                    horizontal = 8.dp,
                                                                    vertical = 4.dp
                                                                ),
                                                            dataStateList = listOf(viewModel.averageData) + viewModel.dataListState,
                                                            selectedIndex = viewModel.selectedDataIndexState.value,
                                                            onDataSelected = { index, _ ->
                                                                viewModel.setSelectedData(index)
                                                                viewModel.setSelectedDay(0) { shouldSlideRight ->
                                                                    slideRight = shouldSlideRight
                                                                }
                                                            }
                                                        )
                                                    }
                                                }
                                                val heightOfPanel by animateDpAsState(
                                                    targetValue =
                                                    if(viewModel.isDayPanelExpanded.value) maxHeight
                                                    else maxHeight - columnHeight,
                                                    animationSpec = tween(
                                                        durationMillis = 400
                                                    )
                                                )
                                                DayWeatherPanel(
                                                    modifier = Modifier
                                                        .align(Alignment.TopCenter)
                                                        .fillMaxWidth()
                                                        .height(heightOfPanel)
                                                        .placeholder(
                                                            visible = viewModel.currentDataState.isLoading,
                                                            color = MaterialTheme.colors.surface.copy(
                                                                alpha = 0.5f
                                                            ),
                                                            shape = RectangleShape,
                                                            highlight = PlaceholderHighlight.shimmer()
                                                        ),
                                                    data = viewModel.selectedDay,
                                                    isExpanded = viewModel.isDayPanelExpanded.value,
                                                    onExpandClick = {
                                                        if(!viewModel.currentDataState.isLoading) {
                                                            viewModel.setDayPanelExpansion(!viewModel.isDayPanelExpanded.value)
                                                        }
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
                                    LaunchedEffect(pageFraction){
                                        if(pageFraction < 1f && viewModel.cardsSelected){
                                            viewModel.clearSelection()
                                        }
                                    }
                                    if(pageState.currentPage == 1){
                                        BackHandler(enabled = true) {
                                            scope.launch {
                                                if(viewModel.cardsSelected) viewModel.clearSelection()
                                                else pageState.animateScrollToPage(0)
                                            }
                                        }
                                    }

                                    //second page
                                    BoxWithConstraints(
                                        modifier = Modifier
                                            .fillMaxSize()
                                    ) {
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxSize(),
                                            contentPadding = if(viewModel.dataListState.isNotEmpty()) PaddingValues(bottom = if(isPortrait) 112.dp else 120.dp)
                                                             else PaddingValues()
                                        ){
                                            item {
                                                DataSourceHeader(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 16.dp),
                                                    action = if(viewModel.cardsSelected) HeaderAction.Delete else HeaderAction.Add,
                                                    onActionClick = {
                                                        if(!viewModel.cardsSelected){
                                                            viewModel.setTemporaryLocation(viewModel.selectedLocation.value)
                                                            viewModel.openSelectLocationSheet(false)
                                                            scope.launch {
                                                                if(!sheetState.isVisible) sheetState.show()
                                                            }
                                                        }
                                                        else{
                                                            viewModel.deleteSelectedData()
                                                        }
                                                    }
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }
                                            if(viewModel.dataListState.isNotEmpty()){
                                                itemsIndexed(viewModel.dataListState){ index, item ->
                                                    DataSourceCard(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(
                                                                horizontal = 8.dp,
                                                                vertical = 4.dp
                                                            ),
                                                        dataState = item,
                                                        isSelected = viewModel.selectedCards.contains(index),
                                                        onLongPress = {
                                                            viewModel.setDataCardSelection(index, true)
                                                        },
                                                        onTap = {
                                                            if(viewModel.cardsSelected){
                                                                viewModel.setDataCardSelection(index, !viewModel.selectedCards.contains(index))
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                            else{
                                                item {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(maxHeight - 90.dp),
                                                        contentAlignment = Alignment.Center
                                                    ){
                                                        Text(
                                                            text = "Please select data sources",
                                                            color = MaterialTheme.colors.primary,
                                                            style = MaterialTheme.typography.h5
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
                    else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ){
                            Text(
                                text = "Please select your location",
                                color = MaterialTheme.colors.primary,
                                style = MaterialTheme.typography.h5
                            )
                        }
                    }
                }
            }

            if(viewModel.dataListState.isNotEmpty()){
                //trust levels panel
                if((pageState.currentPage + pageState.currentPageOffset != 0f) && (viewModel.dataListState.size > 1)){
                    val trustLevelPanelHeight = if(isPortrait) 211 else 118
                    val yOffset = ((trustLevelPanelHeight+2) - (trustLevelPanelHeight * (pageState.currentPage+pageState.currentPageOffset))).dp
                    Card (
                        modifier = Modifier
                            .requiredWidth(maxWidth + 4.dp)
                            .height(trustLevelPanelHeight.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = yOffset)
                            .padding(end = if (!isPortrait) 104.dp else 0.dp),
                        shape = RoundedCornerShape(
                            topStart = if(isPortrait) 20.dp else 0.dp,
                            topEnd = 20.dp
                        ),
                        elevation = 7.dp,
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
                                    .padding(horizontal = 8.dp),
                                dataStateList = viewModel.dataListState,
                                levels = viewModel.trustLevels.value,
                                onLevelChanged = { newLevels ->
                                    viewModel.setTrustLevels(newLevels)
                                }
                            )
                        }
                    }
                }
            }

            if(viewModel.selectedLocation.value.isNotEmpty()){
                //bottom bar
                BottomBar(
                    modifier = Modifier.align(if(isPortrait) Alignment.BottomCenter else Alignment.CenterEnd),
                    pageState = pageState,
                    isHorizontal = isPortrait
                )
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