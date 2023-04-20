package com.bogdan801.weatheraggregator.presentation.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import com.bogdan801.weatheraggregator.R
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bogdan801.weatheraggregator.presentation.screens.home.WeatherDataState

@Composable
fun DataSelector(
    modifier: Modifier = Modifier,
    dataStateList: List<WeatherDataState>,
    selectedIndex: Int,
    onDataSelected: (index: Int, selectedState: WeatherDataState) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ){
            dataStateList.forEachIndexed { index, weatherDataState ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                onDataSelected(index, weatherDataState)
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
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = weatherDataState.data.domain.name,
                            maxLines = 1,
                            color = textColor,
                            overflow = TextOverflow.Ellipsis,
                            style =
                                if(selectedIndex == index) MaterialTheme.typography.body1
                                else MaterialTheme.typography.body2
                        )
                        if(weatherDataState.error != null){
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                modifier = Modifier.size(12.dp),
                                painter = painterResource(id = R.drawable.ic_error),
                                contentDescription = "Error icon",
                                tint = MaterialTheme.colors.error.copy(0.5f)
                            )
                        }
                        if(weatherDataState.isLoading){
                            Spacer(modifier = Modifier.width(4.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.requiredSize(10.dp),
                                strokeWidth = 1.5.dp
                            )
                        }
                    }
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.CenterStart
        ){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(1.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colors.onSurface.copy(0.2f))
            )

            val sliderWidth = 20.dp
            val cellWidth = maxWidth/dataStateList.size
            val targetOffset = ((cellWidth/2) - (sliderWidth/2)) + (cellWidth * selectedIndex)
            val xOffset by animateDpAsState(
                targetValue = targetOffset,
                animationSpec = spring(
                    dampingRatio = 0.7f,
                    stiffness = Spring.StiffnessMedium
                )
            )
            Box(
                modifier = Modifier
                    .size(sliderWidth, 5.dp)
                    .offset(x = xOffset)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colors.onSurface)
            )
        }
    }
}