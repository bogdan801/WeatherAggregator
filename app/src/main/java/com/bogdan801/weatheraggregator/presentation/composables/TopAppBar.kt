package com.bogdan801.weatheraggregator.presentation.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bogdan801.weatheraggregator.R
import com.bogdan801.weatheraggregator.presentation.theme.Theme

@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    onChangeLocationClick: () -> Unit = {},
    getIconCoordinates: (coordinates: Offset) -> Unit = {},
    onChangeThemeClick: () -> Unit = {},
    themeState: State<Theme>,
    isEnabled: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            onClick = onChangeLocationClick,
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

        IconButton(
            modifier = Modifier
                .padding(end = 8.dp)
                .onGloballyPositioned { coordinates ->
                    getIconCoordinates(coordinates.boundsInRoot().center)
                },
            enabled = isEnabled,
            onClick = onChangeThemeClick,
        ) {
            Icon(
                painter = painterResource(
                    id = when(themeState.value){
                        Theme.Auto  -> R.drawable.ic_auto
                        Theme.Light -> R.drawable.ic_light_mode
                        Theme.Dark  -> R.drawable.ic_dark_mode
                    }
                ),
                contentDescription = "",
                tint = MaterialTheme.colors.onSurface
            )
        }
    }
}