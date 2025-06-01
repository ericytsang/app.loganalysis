package com.github.ericytsang.app.ui.frame.workingfileseteditor

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.Colors
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.modal.SettingsDialog

@Composable
fun childWindowManager(
    uniqueKeyPrefix:String,
    onFinalWindowClosed:() -> Unit = {},
):ChildWindowManager
{
    var children by remember { mutableStateOf<Set<@Composable ()->Unit>>(emptySet()) }

    children.forEachIndexed { index,dialog ->
        key("$uniqueKeyPrefix$index") { dialog() }
    }

    return object:ChildWindowManager
    {
        override fun addChildWindow(createChildWindow:@Composable (ChildWindowManagerController)->Unit)
        {
            var function:@Composable ()->Unit = {}
            val childWindowManagerRemote = object:ChildWindowManagerController
            {
                override fun removeSelf()
                {
                    children -= function
                    if (children.isEmpty())
                    {
                        onFinalWindowClosed()
                    }
                }
            }
            function = { createChildWindow(childWindowManagerRemote) }
            children += function
        }
    }
}

/**
 * This interface is used to allow child windows to remove themselves from the ChildWindowManager.
 * It is used to avoid having to pass the ChildWindowManager instance to the child window.
 */
interface ChildWindowManagerController
{
    fun removeSelf()
}

interface ChildWindowManager
{
    fun addChildWindow(createChildWindow:@Composable (ChildWindowManagerController)->Unit)
}

fun ChildWindowManager.showSettingsDialog()
{
    addChildWindow { controller -> SettingsDialog { controller.removeSelf() } }
}

@Composable
fun Modifier.addWindowBarPointerGestureHandling(
    key:Any?,
    window:ComposeWindow,
):Modifier
{
    var rememberOffset by remember { mutableStateOf(Offset.Unspecified) }
    return pointerInput(key) {
        detectDragGestures(
            onDragStart = { offset ->
                rememberOffset = offset
            },
            onDrag = { change, dragAmount ->
                val newX = window.x + change.position.x.dp.roundToPx() - rememberOffset.x.dp.roundToPx()
                val newY = window.y + change.position.y.dp.roundToPx() - rememberOffset.y.dp.roundToPx()
                window.setLocation(newX, newY)
            },
        )
    }
}

@Composable
fun CustomWindowBar(
    onCloseRequest:() -> Unit,
    window:ComposeWindow,
    title:String,
    windowState:WindowState,
    themeColors:Colors,
    content:@Composable RowScope.()->Unit,
)
{
    Row(
        modifier = Modifier
            .background(themeColors.background)
            .addWindowBarPointerGestureHandling(Unit,window),
        verticalAlignment = Alignment.CenterVertically,
    )
    {
        // space at start
        Spacer(modifier = Modifier.defaultMinSize(Dimens.mttPadding))

        // title
        Text(
            text = title,
            modifier = Modifier.weight(1f, fill = true),
            color = themeColors.onSurface,
        )

        // space between title and action buttons
        Spacer(modifier = Modifier.weight(1f, fill = true))

        // custom content
        content()

        // minimize button
        IconButton(
            onClick = { windowState.isMinimized = true },
        ) {
            Icon(
                imageVector = Icons.Default.Menu, // Placeholder icon, replace with minimize icon
                contentDescription = "Minimize",
                tint = themeColors.onSurface,
            )
        }

        // maximize button
        IconButton(
            onClick = { toggleMaximization(windowState) },
        ) {
            Icon(
                imageVector = Icons.Default.Menu, // Placeholder icon, replace with maximize icon
                contentDescription = "Maximize/Restore",
                tint = themeColors.onSurface,
            )
        }

        // close button
        IconButton(
            onClick = onCloseRequest,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = themeColors.onSurface,
            )
        }
    }
}

private fun toggleMaximization(windowState:WindowState)
{
    windowState.placement = when
    {
        windowState.placement != WindowPlacement.Maximized -> WindowPlacement.Maximized
        else -> WindowPlacement.Floating
    }
}


