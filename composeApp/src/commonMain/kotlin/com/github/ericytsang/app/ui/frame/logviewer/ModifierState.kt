package com.github.ericytsang.app.ui.frame.logviewer

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

fun Modifier.captureModifierState(
    onModifierStateChange:(ModifierState)->Unit,
):Modifier = onKeyEvent()
{ keyEvent ->

    // use callback to update the modifier state
    onModifierStateChange(keyEvent.getModifierState())

    // don't consume the event. we just want to track the modifier state, so we can use it when handling clicks
    false
}

fun KeyEvent.isOsAgnosticCtrlPressed():Boolean =
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

fun KeyEvent.getModifierState():ModifierState = ModifierState(
    isOsAgnosticCtrlPressed = isOsAgnosticCtrlPressed(),
    isShiftPressed = isShiftPressed,
    isAltPressed = isAltPressed,
)

data class ModifierState(
    val isOsAgnosticCtrlPressed:Boolean = false,
    val isShiftPressed:Boolean = false,
    val isAltPressed:Boolean = false,
)

fun Modifier.handleKeyCombinations(

    /** e.g. ctrl+a or cmd+a */
    onSelectAll:()->Boolean = { false },

    /** e.g. escape key */
    onDeselectAll:()->Boolean = { false },

    /** e.g. ctrl+c or cmd+c */
    onCopySelection:()->Boolean = { false },

    /** e.g. ctrl+shift+up or cmd+shift+up */
    onExpandSelectionUp:()->Boolean = { false },

    /** e.g. ctrl+shift+down or cmd+shift+down */
    onExpandSelectionDown:()->Boolean = { false },

    /** e.g. up */
    onMoveFocusUp:()->Boolean = { false },

    /** e.g. down */
    onMoveFocusDown:()->Boolean = { false },

) = onKeyEvent { keyEvent ->

    // only act on key down
    if (keyEvent.type != KeyEventType.KeyDown)
        return@onKeyEvent false

    // detect and handle key combinations
    val modifierState = keyEvent.getModifierState()
    val onlyOsAgnosticCtrlPressed = modifierState == ModifierState(isOsAgnosticCtrlPressed = true)
    val onlyShiftPressed = modifierState == ModifierState(isShiftPressed = true)
    when (keyEvent.key)
    {
        // deselect all: escape key
        Key.Escape -> onDeselectAll()

        // select all: ctrl+a or cmd+a
        Key.A if (onlyOsAgnosticCtrlPressed) -> onSelectAll()

        // copy selection: ctrl+c or cmd+c
        Key.C if (onlyOsAgnosticCtrlPressed) -> onCopySelection()

        // expand selection up: ctrl+shift+up or cmd+shift+up
        Key.DirectionUp -> if (onlyShiftPressed) onExpandSelectionUp() else onMoveFocusUp()

        // expand selection down: ctrl+shift+down or cmd+shift+down
        Key.DirectionDown -> if (onlyShiftPressed) onExpandSelectionDown() else onMoveFocusDown()

        // don't handle other keys
        else -> false
    }
}
