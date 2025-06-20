package com.github.ericytsang.app.ui.frame.logviewer

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.onKeyEvent

fun Modifier.captureModifierState(
    onModifierStateChange:(ModifierState)->Unit,
):Modifier = onKeyEvent()
{ keyEvent ->

    // for macOS, the meta key is the command key, and for Windows/Linux, it is the control key
    val isMultiSelectModifierPressed = keyEvent.isMultiSelectPressed()
    val isRangeSelectModifierPressed = keyEvent.isShiftPressed

    // use callback to update the modifier state
    onModifierStateChange(
        ModifierState(
            isMultiSelectModifierPressed = isMultiSelectModifierPressed,
            isRangeSelectModifierPressed = isRangeSelectModifierPressed,
        )
    )

    // don't consume the event. we just want to track the modifier state, so we can use it when handling clicks
    false
}

fun KeyEvent.isMultiSelectPressed():Boolean =
    if (System.getProperty("os.name").contains("Mac",ignoreCase = true))
    {
        // Use Command (Meta) key
        isMetaPressed
    }
    else
    {
        // Use Control key
        isCtrlPressed
    }

data class ModifierState(
    val isMultiSelectModifierPressed:Boolean = false,
    val isRangeSelectModifierPressed:Boolean = false,
)
