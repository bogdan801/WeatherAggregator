package com.bogdan801.weatheraggregator.presentation.composables

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import com.bogdan801.weatheraggregator.domain.model.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bogdan801.weatheraggregator.R
import com.bogdan801.weatheraggregator.data.util.toDegree
import com.bogdan801.weatheraggregator.data.util.toFormattedString
import com.bogdan801.weatheraggregator.domain.model.WeatherData
import java.time.DayOfWeek

@Composable
fun WeatherDataViewer(
    modifier: Modifier = Modifier,
    data: WeatherData,
    isLoading: Boolean = data.weatherByDates.isEmpty(),
    error: String? = null
){
    if(error!=null){
        Box(
            modifier = modifier
                .height(340.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color = Color.Gray.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ){
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    modifier = Modifier.size(48.dp),
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Error",
                    tint = Color.Red
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = error, color = Color.Red)
            }
        }
    }
    else if (isLoading){
        val shimmerColors = remember {
            listOf(
                Color.LightGray.copy(alpha = 0.6f),
                Color.LightGray.copy(alpha = 0.2f),
                Color.LightGray.copy(alpha = 0.6f)
            )
        }
        
        val transition = rememberInfiniteTransition()
        val transitionAnimation = transition.animateFloat(
            initialValue = -400f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000,
                    easing = LinearEasing
                )
            )
        )

        val brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(transitionAnimation.value, transitionAnimation.value),
            end = Offset(transitionAnimation.value + 400f, transitionAnimation.value + 400f)
        )
        
        Box(
            modifier = modifier
                .height(340.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(brush),
            contentAlignment = Alignment.Center
        ){
            CircularProgressIndicator(color = Color(0xFF118ABB))
        }
    }
    else if(!data.isEmpty) {
        var currentSkyCondition by remember{ mutableStateOf(data.currentSkyCondition)}
        var date by remember{ mutableStateOf(data.currentDate)}
        var currentTemperature by remember{ mutableStateOf(data.currentTemperature)}
        var selectedDayIndex by remember{ mutableStateOf(0)}
        Column(
            modifier = modifier
                .background(Color.White)
                .padding(horizontal = 8.dp)
                .height(340.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.size(96.dp),
                    painter = currentSkyCondition.getPainterResource(),
                    contentDescription = ""
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Погода ")
                        Image(
                            modifier = Modifier
                                .size(16.dp),
                            painter = when(data.domain){
                                WeatherSourceDomain.Meta -> painterResource(R.drawable.ic_meta)
                                WeatherSourceDomain.Sinoptik -> painterResource(R.drawable.ic_sinoptik)
                                WeatherSourceDomain.OpenWeather -> painterResource(R.drawable.ic_open_weather)
                                else -> painterResource(R.drawable.ic_average)
                            },
                            contentDescription = ""
                        )
                    }
                    Text(text = "${data.currentLocation} • ${date.toFormattedString()}")
                    Text(
                        text = currentTemperature.toDegree(),
                        fontSize = 36.sp
                    )
                }
            }
            Row(modifier = Modifier
                .widthIn(max = 400.dp)
                .padding(vertical = 4.dp, horizontal = 4.dp)
            ) {
                val spacing = 2.dp
                Column(modifier = Modifier
                    .width(60.dp)
                ) {
                    Text(modifier = Modifier.padding(vertical = spacing), text = "Час",          fontSize = 10.sp)
                    Text(modifier = Modifier.padding(vertical = spacing), text = "Хмарність",    fontSize = 10.sp)
                    Text(modifier = Modifier.padding(vertical = spacing), text = "Температура",  fontSize = 10.sp)
                    Text(modifier = Modifier.padding(vertical = spacing), text = "Опади, %",     fontSize = 10.sp)
                    Text(modifier = Modifier.padding(vertical = spacing), text = "Тиск, мм",     fontSize = 10.sp)
                    Text(modifier = Modifier.padding(vertical = spacing), text = "Вологість, %", fontSize = 10.sp)
                    Text(modifier = Modifier.padding(vertical = spacing), text = "Вітер, м/с",   fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.width(4.dp))
                data.weatherByDates[selectedDayIndex].weatherByHours.forEachIndexed { index, slice->
                    Column(modifier = Modifier
                        .weight(1f)
                        .background(if (index % 2 == 0) Color(0xFFF3FAFD) else Color.White),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(modifier = Modifier.padding(vertical = spacing), text = slice.time, fontSize = 10.sp)
                        Image(
                            modifier = Modifier
                                .padding(2.dp)
                                .size(16.dp),
                            painter = slice.skyCondition.getPainterResource(),
                            contentDescription = ""
                        )
                        Text(modifier = Modifier.padding(vertical = spacing), text = slice.temperature.toDegree(), fontSize = 10.sp)
                        Text(modifier = Modifier.padding(vertical = spacing), text = if(slice.precipitationProbability>=0) slice.precipitationProbability.toString() else "-", fontSize = 10.sp)
                        Text(modifier = Modifier.padding(vertical = spacing), text = slice.pressure.toString(), fontSize = 10.sp)
                        Text(modifier = Modifier.padding(vertical = spacing), text = slice.humidity.toString(), fontSize = 10.sp)
                        Row(verticalAlignment = Alignment.CenterVertically){
                            Icon(
                                modifier = Modifier
                                    .width(8.dp)
                                    .aspectRatio(3f / 2)
                                    .rotate(
                                        when (slice.wind) {
                                            is Wind.East -> 0f
                                            is Wind.SouthEast -> 45f
                                            is Wind.South -> 90f
                                            is Wind.SouthWest -> 135f
                                            is Wind.West -> 180f
                                            is Wind.NorthWest -> 225f
                                            is Wind.North -> 270f
                                            is Wind.NorthEast -> 315f
                                        }
                                    ),
                                painter = painterResource(id = R.drawable.ic_arrow),
                                contentDescription = "wind",
                                tint = Color(0xFF5C8599)
                            )
                            Text(
                                modifier = Modifier
                                    .padding(vertical = spacing),
                                text = " " + when(slice.wind){
                                    is Wind.East      -> slice.wind.power.toString()
                                    is Wind.North     -> slice.wind.power.toString()
                                    is Wind.NorthEast -> slice.wind.power.toString()
                                    is Wind.NorthWest -> slice.wind.power.toString()
                                    is Wind.South     -> slice.wind.power.toString()
                                    is Wind.SouthEast -> slice.wind.power.toString()
                                    is Wind.SouthWest -> slice.wind.power.toString()
                                    is Wind.West      -> slice.wind.power.toString()
                                },
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            BoxWithConstraints(modifier = Modifier
                .padding(top = 8.dp)
                .widthIn(max = 400.dp)
                .height(75.dp)
            ) {
                val scope = this
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    data.weatherByDates.forEachIndexed { index, day ->
                        Column(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .width((scope.maxWidth - 40.dp) / 5)
                                .aspectRatio(1f)
                                .background(
                                    if (selectedDayIndex == index)
                                        Color(0xFFE3ECF7)
                                    else Color(0xFFF1F7FA)
                                )
                                .clickable {
                                    selectedDayIndex = index
                                    if (index == 0) {
                                        currentSkyCondition = data.currentSkyCondition
                                        currentTemperature = data.currentTemperature
                                        date = data.currentDate
                                    } else {
                                        currentSkyCondition = day.skyCondition
                                        currentTemperature = day.dayTemperature
                                        date = day.date
                                    }
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            Column(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .weight(0.4f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val red = Color(0xFFE73C3C)
                                Text(
                                    text = when(day.date.dayOfWeek){
                                        DayOfWeek.MONDAY    -> "Понеділок"
                                        DayOfWeek.TUESDAY   -> "Вівторок"
                                        DayOfWeek.WEDNESDAY -> "Середа"
                                        DayOfWeek.THURSDAY  -> "Четвер"
                                        DayOfWeek.FRIDAY    -> "П'ятниця"
                                        DayOfWeek.SATURDAY  -> "Субота"
                                        DayOfWeek.SUNDAY    -> "Неділя"
                                    },
                                    fontSize = 10.sp,
                                    color = when(day.date.dayOfWeek){
                                        DayOfWeek.SATURDAY -> red
                                        DayOfWeek.SUNDAY   -> red
                                        else               -> Color.Black
                                    }
                                )
                                Text(
                                    modifier = Modifier.offset(y = (-2).dp),
                                    text = day.date.toFormattedString(),
                                    fontSize = 8.sp,
                                    color = when(day.date.dayOfWeek){
                                        DayOfWeek.SATURDAY -> red
                                        DayOfWeek.SUNDAY   -> red
                                        else               -> Color.Black
                                    }
                                )
                            }
                            Image(
                                modifier = Modifier
                                    .size(24.dp)
                                    .weight(0.3f),
                                painter = day.skyCondition.getPainterResource(),
                                contentDescription = ""
                            )
                            Text(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .weight(0.3f),
                                text = day.nightTemperature.toDegree() + "  " + day.dayTemperature.toDegree(),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
    else{
        Box(
            modifier = modifier
                .height(340.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Gray.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ){
            Text(text = "Дані відсутні")
        }
    }
}
