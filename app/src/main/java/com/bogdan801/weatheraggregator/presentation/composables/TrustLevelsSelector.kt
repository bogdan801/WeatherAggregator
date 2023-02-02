package com.bogdan801.weatheraggregator.presentation.composables

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bogdan801.weatheraggregator.presentation.screens.home.WeatherDataState

@Composable
fun TrustLevelsSelector(
    modifier: Modifier = Modifier,
    dataStateList: List<WeatherDataState>,
    levels: List<Double>,
    onLevelChanged: (List<Double>) -> Unit
) {
    val density = LocalDensity.current
    val listWithoutAverage = dataStateList.subList(1, dataStateList.lastIndex+1)
    BoxWithConstraints(modifier = modifier) {
        val wholeWidth = maxWidth

        val sectionsWidth = remember {
            mutableStateListOf(
                *(levels.map { level ->
                    maxWidth * level.toFloat()
                }
                .toTypedArray())
            )
        }

        val handlesOffsets = remember {
            mutableStateListOf(
                *(
                    List(levels.subList(0, levels.lastIndex).size) { index ->
                        maxWidth * levels.subList(0, index + 1).sum().toFloat()
                    }.toTypedArray()
                )
            )
        }

        val percentages = remember {
            mutableStateListOf(
                *(levels.map { level -> "%,.0f".format((level * 100)) }.toTypedArray())
            )
        }

        Row(modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.small)
        ) {
            listWithoutAverage.forEachIndexed { index, weatherDataState ->
                Column(
                    modifier = Modifier
                        .background(
                            if (index % 2 == 0) MaterialTheme.colors.onPrimary
                            else MaterialTheme.colors.onPrimary.copy(0.6f)
                        )
                        .fillMaxHeight()
                        .width(sectionsWidth[index]),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(
                        text = weatherDataState.data.domain.name,
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = percentages[index] + "%",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        handlesOffsets.forEachIndexed { index, offset ->
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            var isDragged by remember {
                mutableStateOf(false)
            }
            val handleWidth by animateDpAsState(
                targetValue = if(isDragged || isPressed) 11.dp else 1.dp
            )

            Box(
                modifier = Modifier
                    .width(handleWidth)
                    .offset(offset - (handleWidth / 2))
                    .fillMaxHeight()
                    .background(MaterialTheme.colors.surface)
                    .clickable(interactionSource = interactionSource, indication = null, onClick = {})
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            val d = with(density) { delta.toDp() }

                            val levelLeft = (sectionsWidth[index] + d).value.toDouble() / wholeWidth.value
                            val levelRight = (sectionsWidth[index + 1] - d).value.toDouble() / wholeWidth.value

                            if(levelLeft > 0.1 && levelRight > 0.1){
                                percentages[index] = "%,.0f".format(levelLeft * 100)
                                percentages[index + 1] = "%,.0f".format(levelRight * 100)

                                handlesOffsets[index] += d
                                sectionsWidth[index] += d
                                sectionsWidth[index + 1] -= d
                            }
                        },
                        onDragStarted = {
                            isDragged = true
                        },
                        onDragStopped = {
                            isDragged = false
                            val newLevels = sectionsWidth.map { sectionWidth ->
                                sectionWidth.value.toDouble() / wholeWidth.value
                            }
                            onLevelChanged(newLevels)
                        }
                    ),
                contentAlignment = Alignment.Center
            ){
                Box(modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .padding(vertical = 3.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colors.primary.copy(0.5f))
                )
            }
        }
    }
}