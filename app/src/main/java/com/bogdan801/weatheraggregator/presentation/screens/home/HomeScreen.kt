package com.bogdan801.weatheraggregator.presentation.screens.home

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.applyCanvas
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.bogdan801.weatheraggregator.R
import com.bogdan801.weatheraggregator.presentation.theme.Theme
import com.bogdan801.weatheraggregator.presentation.theme.WeatherAggregatorTheme
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavHostController? = null,
    viewModel: HomeScreenViewModel = hiltViewModel()
){
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    val scaffoldState = rememberScaffoldState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        snackbarHost = {scaffoldState.snackbarHostState}
    ) {
        //states for animated theme transition
        val imageState = remember { mutableStateOf<ImageBitmap?>(null) }
        val iconCoordinates = remember { mutableStateOf(Offset.Zero) }
        val isAnimating = remember { mutableStateOf(false)}
        val animatable = remember { Animatable(50f) }

        Column(modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colors.secondary,
                        if ((viewModel.themeState.value == Theme.Dark) ||
                            (viewModel.themeState.value == Theme.Auto && isSystemInDarkTheme())
                        ) MaterialTheme.colors.secondary
                        else MaterialTheme.colors.primaryVariant
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    modifier = Modifier.padding(8.dp),
                    onClick = { /*TODO*/ },
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.secondary,
                        contentColor = MaterialTheme.colors.onSurface
                    ),
                    elevation = null,
                    border = BorderStroke(width = 1.dp, color = Color.White.copy(alpha = 0.5f))
                ) {
                    Row {
                        Icon(painter = painterResource(id = R.drawable.ic_location), contentDescription = "location")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Change location", style = MaterialTheme.typography.h4)
                    }
                }

                var count by remember { mutableStateOf(viewModel.themeState.value.ordinal) }
                var isEnabled by remember { mutableStateOf(true) }
                IconButton(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .onGloballyPositioned { coordinates ->
                            iconCoordinates.value = coordinates.boundsInRoot().center
                        },
                    enabled = isEnabled,
                    onClick = {
                        count++
                        viewModel.setTheme(count % 3, context)

                        //animated transition between themes
                        coroutineScope.launch {
                            isEnabled = false

                            //save screenshot
                            val bmp = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).applyCanvas {
                                view.draw(this)
                            }
                            imageState.value = bmp.asImageBitmap()

                            isAnimating.value = true
                            animatable.animateTo(
                                2500f,
                                tween(350)
                            )
                            isAnimating.value = false

                            animatable.snapTo(50f)
                            isEnabled = true
                        }
                    },
                ) {
                    Icon(
                        painter = painterResource(
                            id = when(viewModel.themeState.value){
                                Theme.Auto -> R.drawable.ic_auto
                                Theme.Light -> R.drawable.ic_light_mode
                                Theme.Dark -> R.drawable.ic_dark_mode
                            }
                        ),
                        contentDescription = "",
                        tint = MaterialTheme.colors.onSurface
                    )
                }
            }
        }

        /////CONTENT//////


        /////////////////

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

@Composable
@Preview
fun PreviewMain() {
    WeatherAggregatorTheme(Theme.Light) {

    }
}