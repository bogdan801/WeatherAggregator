package com.bogdan801.weatheraggregator.presentation.composables

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bogdan801.weatheraggregator.R
import com.bogdan801.weatheraggregator.domain.model.Location
import com.bogdan801.weatheraggregator.presentation.screens.home.SelectLocationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SelectLocationSheet(
    modifier: Modifier = Modifier,
    viewModel: SelectLocationViewModel = hiltViewModel(),
    sheetState: ModalBottomSheetState,
    onLocationSelected: (Location) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = sheetState.isVisible){
        if(!sheetState.isVisible){
            viewModel.displayOblastList()
        }
    }

    Column(modifier = modifier){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            SearchBar(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
                value = viewModel.searchBarText.value,
                onValueChange = { newText ->
                    viewModel.searchBarTextChanged(newText)
                },
                onSearch = {
                    this.defaultKeyboardAction(ImeAction.Search)
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.typeLocation),
                        color = MaterialTheme.colors.primary,
                        style = MaterialTheme.typography.subtitle1
                    )
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(60.dp),
                contentAlignment = Alignment.Center
            ){
                IconButton(
                    modifier = Modifier.offset(x = (-2).dp),
                    onClick = {
                        if(viewModel.searchBarText.value.isNotBlank()) viewModel.searchBarTextChanged("")
                        else scope.launch { sheetState.hide() }
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(id = R.drawable.ic_cancel),
                        contentDescription = "",
                        tint = MaterialTheme.colors.primary
                    )
                }
            }
        }


        AnimatedContent(
            targetState = viewModel.searchBarText.value.isBlank(),
            transitionSpec = {
                fadeIn() with fadeOut()
            }
        ) { showSelection ->
            Column(modifier = Modifier.fillMaxSize()) {
                if(showSelection){
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                        state = viewModel.lazyRowState,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        itemsIndexed(viewModel.path.value){ index, name ->
                            Text(
                                modifier = Modifier.clickable(
                                    onClick = {
                                        when(index){
                                            0 -> viewModel.displayOblastList()
                                            1 -> viewModel.selectOblast(name)
                                            2 -> viewModel.selectRegion(viewModel.path.value[1], name)
                                        }
                                        scope.launch {
                                            delay(200)
                                            viewModel.lazyColumnState.scrollToItem(0)
                                        }
                                    },
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ),
                                text = name + when(index){
                                    1 -> if(name != "Автономна Республіка Крим") " область" else ""
                                    2 -> " район"
                                    else -> ""
                                },
                                color = MaterialTheme.colors.onSurface,
                                style = MaterialTheme.typography.h5
                            )
                            if(index != viewModel.path.value.lastIndex){
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    modifier = Modifier
                                        .size(13.dp)
                                        .rotate(270f),
                                    painter = painterResource(id = R.drawable.ic_expand),
                                    contentDescription = "",
                                    tint = MaterialTheme.colors.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp, top = 4.dp)
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colors.primary.copy(0.3f))
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = viewModel.lazyColumnState
                    ){
                        items(viewModel.selectionDisplayList.value){ settlement ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .clickable {
                                        when (viewModel.path.value.size) {
                                            1 -> {
                                                viewModel.selectOblast(settlement)
                                            }
                                            2 -> {
                                                viewModel.selectRegion(
                                                    viewModel.path.value[1],
                                                    settlement
                                                )
                                            }
                                            3 -> {
                                                onLocationSelected(
                                                    viewModel.selectLocation(
                                                        viewModel.path.value[1],
                                                        viewModel.path.value[2],
                                                        settlement
                                                    )
                                                )
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ){
                                Text(
                                    text = settlement,
                                    color = MaterialTheme.colors.onSurface,
                                    style = MaterialTheme.typography.subtitle1
                                )
                            }
                        }
                    }
                }
                else {
                    LazyColumn(modifier = Modifier.fillMaxSize()){
                        items(viewModel.foundLocations.value){ location ->
                            FoundItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                data = location,
                                onItemSelected = {
                                    onLocationSelected(
                                        viewModel.selectLocation(
                                            location.oblastName,
                                            location.regionName,
                                            location.name
                                        )
                                    )
                                    scope.launch {
                                        delay(100)
                                        viewModel.lazyColumnState.scrollToItem(0)
                                        viewModel.lazyRowState.scrollToItem(viewModel.path.value.lastIndex)
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    }
                                }
                            )
                        }
                        items(viewModel.foundRegions.value){ region ->
                            FoundItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                data = region,
                                onItemSelected = {
                                    viewModel.selectRegion(region.oblastName, region.regionName)
                                    scope.launch {
                                        delay(100)
                                        viewModel.lazyColumnState.scrollToItem(0)
                                        viewModel.lazyRowState.scrollToItem(viewModel.path.value.lastIndex)
                                    }
                                }
                            )
                        }
                        items(viewModel.foundOblasts.value){ oblast ->
                            FoundItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                data = oblast,
                                onItemSelected = {
                                    viewModel.selectOblast(oblast.oblastName)
                                    scope.launch {
                                        delay(100)
                                        viewModel.lazyColumnState.scrollToItem(0)
                                        viewModel.lazyRowState.scrollToItem(viewModel.path.value.lastIndex)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoundItem(
    modifier: Modifier = Modifier,
    data: Location,
    onItemSelected: () -> Unit = {}
) {
    Column(modifier = modifier.clickable { onItemSelected() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colors.primary.copy(0.3f))
        )
        LazyRow(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            if(data.name.isNotBlank()){
                item {
                    Text(
                        text = data.name,
                        color = MaterialTheme.colors.onSurface,
                        style = MaterialTheme.typography.h5
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        modifier = Modifier
                            .size(13.dp)
                            .rotate(90f),
                        painter = painterResource(id = R.drawable.ic_expand),
                        contentDescription = "",
                        tint = MaterialTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
            if(data.regionName.isNotBlank()){
                item {
                    Text(
                        text = data.regionName + " район",
                        color = MaterialTheme.colors.onSurface,
                        style = MaterialTheme.typography.h5
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        modifier = Modifier
                            .size(13.dp)
                            .rotate(90f),
                        painter = painterResource(id = R.drawable.ic_expand),
                        contentDescription = "",
                        tint = MaterialTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
            item {
                Text(
                    text = data.oblastName + if(data.oblastName != "Автономна Республіка Крим") " область" else "",
                    color = MaterialTheme.colors.onSurface,
                    style = MaterialTheme.typography.h5
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colors.primary.copy(0.3f))
        )
    }
}