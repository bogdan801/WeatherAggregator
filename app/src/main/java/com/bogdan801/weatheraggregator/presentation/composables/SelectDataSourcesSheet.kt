package com.bogdan801.weatheraggregator.presentation.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bogdan801.weatheraggregator.R
import com.bogdan801.weatheraggregator.domain.model.Location
import com.bogdan801.weatheraggregator.domain.model.WeatherSourceDomain
import okhttp3.internal.toImmutableList

@Composable
fun SelectDataSourcesSheet(
    modifier: Modifier = Modifier,
    location: Location,
    onSourcesSelected: (List<WeatherSourceDomain>) -> Unit = {},
    selectedDomains: List<WeatherSourceDomain> = listOf()
) {
    val dataSourcesList by remember {
        val list = WeatherSourceDomain.values().filter { source ->
            when(source){
                WeatherSourceDomain.Meta -> true
                WeatherSourceDomain.Sinoptik -> location.sinoptikLink.isNotBlank()
                WeatherSourceDomain.OpenWeather -> location.lat > 0 && location.lon >0
                WeatherSourceDomain.Average -> false
            }
        }.toList()
        mutableStateOf(list)
    }

    var selectionList by rememberSaveable{
        mutableStateOf(dataSourcesList.map{ selectedDomains.contains(it) })
    }

    val isButtonEnabled by remember {
        derivedStateOf {
            selectionList.contains(true)
        }
    }

    val updateItemState = { id: Int ->
        val newList = selectionList.toMutableList().apply { this[id] = !this[id] }.toImmutableList()
        selectionList = newList
    }

    Column(
        modifier = modifier,
    ){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            contentAlignment = Alignment.Center
        ){
            Text(
                text = "Select data sources",
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.onSurface
            )
        }
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
                .height(0.5.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colors.onSurface.copy(alpha = 0.3f))
        )
        Box(modifier = Modifier.fillMaxSize()){
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ){
                itemsIndexed(dataSourcesList){id, source ->
                    DataSourceItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        domain = source,
                        state = selectionList[id],
                        onStateChanged = { updateItemState(id) }
                    )
                }
            }

            Button(
                modifier = Modifier
                    .padding(bottom = 58.dp)
                    .size(190.dp, 40.dp)
                    .align(Alignment.BottomCenter),
                onClick = {
                    val outputList = dataSourcesList.filterIndexed { index, _ ->
                        selectionList[index]
                    }
                    onSourcesSelected(outputList)
                },
                shape = MaterialTheme.shapes.medium,
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 5.dp,
                    pressedElevation = 7.dp,
                    disabledElevation = 0.dp
                ),
                enabled = isButtonEnabled
            ) {
                Text(
                    text = "SELECT",
                    style = MaterialTheme.typography.body2,
                    color = if(isButtonEnabled) MaterialTheme.colors.onSecondary else MaterialTheme.colors.onSurface
                )
            }
        }
    }
}

@Composable
fun DataSourceItem(
    modifier: Modifier = Modifier,
    domain: WeatherSourceDomain = WeatherSourceDomain.Meta,
    state: Boolean = false,
    onStateChanged: (prevState: Boolean) -> Unit = {}
) {
    val colorState by animateColorAsState(
        targetValue = if(state) MaterialTheme.colors.secondary else MaterialTheme.colors.onPrimary,
        animationSpec = tween(200)
    )
    Row(
        modifier = modifier
            .background(colorState)
            .clickable(
                interactionSource = remember{ MutableInteractionSource() },
                indication = null,
                onClick = {
                    onStateChanged(state)
                }
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(80.dp),
            contentAlignment = Alignment.Center
        ){
            Image(
                modifier = Modifier.size(48.dp),
                painter = when(domain){
                    WeatherSourceDomain.Meta -> painterResource(id = R.drawable.ic_meta)
                    WeatherSourceDomain.Sinoptik -> painterResource(id = R.drawable.ic_sinoptik)
                    WeatherSourceDomain.OpenWeather -> painterResource(id = R.drawable.ic_open_weather)
                    WeatherSourceDomain.Average -> painterResource(id = R.drawable.ic_average)
                },
                contentDescription = null
            )
        }
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ){
            Column {
                Text(
                    text = domain.toString(),
                    color = MaterialTheme.colors.onSurface,
                    style = MaterialTheme.typography.overline
                )
                Text(
                    text = domain.domain,
                    color = MaterialTheme.colors.onSurface.copy(0.65f),
                    style = MaterialTheme.typography.caption
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(80.dp),
            contentAlignment = Alignment.Center
        ){
            Checkbox(
                modifier = Modifier.align(Alignment.Center),
                checked = state,
                onCheckedChange = onStateChanged,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colors.primary,
                    checkmarkColor = MaterialTheme.colors.onPrimary,
                    uncheckedColor = MaterialTheme.colors.primary
                )
            )
        }
        
    }
}