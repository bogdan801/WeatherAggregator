package com.bogdan801.weatheraggregator.presentation.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bogdan801.weatheraggregator.R
import com.bogdan801.weatheraggregator.domain.model.Location
import com.bogdan801.weatheraggregator.presentation.screens.home.SelectLocationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
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
            
/*            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colors.primary),
                elevation = 0.dp,
                backgroundColor = MaterialTheme.colors.onSecondary,
                shape = RoundedCornerShape(12.dp)
            ) {
                SearchBar(
                    modifier = Modifier.fillMaxSize(),
                    value = viewModel.searchBarText.value,
                    onValueChange = { newText ->
                        viewModel.searchBarTextChanged(newText)
                    },
                    onSearch = {
                        this.defaultKeyboardAction(ImeAction.Search)
                    }
                )
            }*/

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
    }
}