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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bogdan801.weatheraggregator.R
import com.bogdan801.weatheraggregator.domain.model.Location
import com.bogdan801.weatheraggregator.presentation.screens.home.SelectLocationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun SelectLocationSheet(
    modifier: Modifier = Modifier,
    viewModel: SelectLocationViewModel = hiltViewModel(),
    sheetState: BottomSheetState,
    onLocationSelected: (Location) -> Unit = {}
) {
    val scope = rememberCoroutineScope()

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
                        text = "Type location...",
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
                        else scope.launch { sheetState.collapse() }
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
                    val lazyColumnState = rememberLazyListState()
                    val lazyRowState = rememberLazyListState()
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                        state = lazyRowState,
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
                                        scope.launch { lazyColumnState.scrollToItem(0) }
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
                        state = lazyColumnState
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
                                                scope.launch {
                                                    lazyColumnState.scrollToItem(0)
                                                    delay(100)
                                                    lazyRowState.scrollToItem(viewModel.path.value.lastIndex)
                                                }
                                            }
                                            2 -> {
                                                viewModel.selectRegion(viewModel.path.value[1], settlement)
                                                scope.launch {
                                                    lazyColumnState.scrollToItem(0)
                                                    delay(100)
                                                    lazyRowState.scrollToItem(viewModel.path.value.lastIndex)
                                                }
                                            }
                                            3 -> {
                                                onLocationSelected(viewModel.selectLocation(settlement))
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
            }
        }
    }
}