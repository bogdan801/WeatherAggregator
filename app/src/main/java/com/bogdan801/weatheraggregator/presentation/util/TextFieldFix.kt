package com.bogdan801.weatheraggregator.presentation.util

import android.annotation.SuppressLint
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.input.TextFieldValue

val osProperty: String = System.getProperty("os.name", "unknown") as String
val isMac = osProperty.startsWith("mac", ignoreCase = true)

@SuppressLint("UnnecessaryComposedModifier")
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.applyTextFieldFixes(value: TextFieldValue): Modifier = composed {
    onPreviewKeyEvent { event ->
        // Returns true to mark the event as handled and stop its propagation.
        when {
            // Temporary fix for https://github.com/JetBrains/compose-jb/issues/565.
            // To repro, press CTRL/Option+backspace on an empty TextField.
            event.isCtrlBackspace() && value.text.isEmpty() -> true
            // Temporary fix for https://github.com/JetBrains/compose-jb/issues/2023.
            // To repro, simply press the DEL key at the end of the text.
            event.key == Key.Delete && value.isCursorAtTheEnd() -> true
            else -> false
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun KeyEvent.isCtrlBackspace() = key == Key.Backspace && (isCtrlPressed || (isMac && isAltPressed))

private fun TextFieldValue.isCursorAtTheEnd(): Boolean {
    val hasNoSelection = selection.collapsed
    val isCursorAtTheEnd = text.length == selection.end
    return hasNoSelection && isCursorAtTheEnd
}

