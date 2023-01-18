package com.bogdan801.weatheraggregator.presentation.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bogdan801.weatheraggregator.data.util.toDegree
import com.bogdan801.weatheraggregator.data.util.toFormattedDate
import com.bogdan801.weatheraggregator.domain.model.SkyCondition
import kotlinx.datetime.LocalDate

@Composable
fun DayCard(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onCardClick: () -> Unit = {},
    date: LocalDate = LocalDate(2023, 1, 6),
    skyCondition: SkyCondition,
    nightTemperature: Int,
    dayTemperature: Int
) {
    val context = LocalContext.current
    val elevation by animateDpAsState(
        targetValue = if(isSelected) 3.dp else 0.dp,
        tween(200)
    )
    val backgroundColor by animateColorAsState(
        targetValue = if(isSelected) MaterialTheme.colors.surface
                      else MaterialTheme.colors.onBackground,
        tween(200)
    )
    val contentColor by animateColorAsState(
        targetValue = if(isSelected) MaterialTheme.colors.onSurface
                      else MaterialTheme.colors.onSurface.copy(alpha = 0.65f),
        tween(200)
    )
    val borderColor by animateColorAsState(
        targetValue = if(isSelected) Color.White.copy(alpha = 0f)
        else Color.White.copy(alpha = 0.5f),
        tween(200)
    )
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = elevation,
        border = BorderStroke(1.dp, borderColor),
        backgroundColor = backgroundColor,
        contentColor = contentColor
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(color = Color.White.copy(alpha = 0.3f)),
                    onClick = onCardClick
                )
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = date.toFormattedDate(context),
                    style = MaterialTheme.typography.caption
                )
            }
            BoxWithConstraints(modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f),
                contentAlignment = Alignment.Center
            ){
                Image(
                    modifier = Modifier.size(maxHeight),
                    painter = skyCondition.getPainterResource(),
                    contentDescription = "Day Weather Icon"
                )
            }
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f),
                contentAlignment = Alignment.Center
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = nightTemperature.toDegree(), style = MaterialTheme.typography.caption)
                    Text(text = dayTemperature.toDegree(), style = MaterialTheme.typography.caption)
                }
            }
        }
    }
}