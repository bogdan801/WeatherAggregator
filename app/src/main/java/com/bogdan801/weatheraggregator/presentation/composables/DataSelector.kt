package com.bogdan801.weatheraggregator.presentation.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bogdan801.weatheraggregator.presentation.screens.home.WeatherDataState

@Composable
fun DataSelector(
    modifier: Modifier = Modifier,
    dataStateList: List<WeatherDataState>,
    selectedIndex: Int,
    onDataSelected: (index: Int, isError: Boolean) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ){
            dataStateList.forEachIndexed { index, weatherDataState ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                onDataSelected(index, weatherDataState.error != null)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ){
                    val textColor by animateColorAsState(
                        targetValue =
                            if(selectedIndex == index) MaterialTheme.colors.onSurface
                            else MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                        animationSpec = tween(200)
                    )

                    Text(
                        text = weatherDataState.data.domain.name,
                        maxLines = 1,
                        color = textColor,
                        style =
                            if(selectedIndex == index) MaterialTheme.typography.body1
                            else MaterialTheme.typography.body2
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(1.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colors.onSurface.copy(0.2f))
        )
    }
}