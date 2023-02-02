package com.bogdan801.weatheraggregator.presentation.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DataSourceHeader(
    modifier: Modifier = Modifier,
    action: HeaderAction = HeaderAction.Add,
    onActionClick: (HeaderAction) -> Unit = {}
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Data sources",
            color = MaterialTheme.colors.onSurface,
            style = MaterialTheme.typography.h3
        )
        CompositionLocalProvider(LocalRippleTheme provides  RippleCustomTheme) {
            Button(
                modifier = Modifier.size(65.dp, 37.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onSecondary
                ),
                shape = MaterialTheme.shapes.medium,
                onClick = {
                    onActionClick(action)
                }
            ) {
                Icon(
                    imageVector = when(action){
                        HeaderAction.Add -> Icons.Default.Add
                        HeaderAction.Delete -> Icons.Default.Delete
                    },
                    tint = MaterialTheme.colors.onSecondary,
                    contentDescription = "Add/Delete data source"
                )
            }
        }
    }
}

enum class HeaderAction{
    Add,
    Delete
}

private object RippleCustomTheme: RippleTheme {
    @Composable
    override fun defaultColor() =
        RippleTheme.defaultRippleColor(
            MaterialTheme.colors.onSecondary,
            lightTheme = true
        )

    @Composable
    override fun rippleAlpha(): RippleAlpha =
        RippleTheme.defaultRippleAlpha(
            Color.Black,
            lightTheme = true
        )
}
