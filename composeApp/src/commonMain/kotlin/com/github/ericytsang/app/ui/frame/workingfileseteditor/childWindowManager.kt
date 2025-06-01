package com.github.ericytsang.app.ui.frame.workingfileseteditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.ericytsang.app.ui.modal.SettingsDialog

data class ChildWindow(
    val uniqueKey:Any,
    val composable:@Composable () -> Unit,
)

@Composable
fun childWindowManager(
    onFinalWindowClosed:() -> Unit = {},
):ChildWindowManager
{
    var children by remember { mutableStateOf<Set<ChildWindow>>(emptySet()) }
    val uniqueKeyGenerator = remember { generateSequence(0) { it+1 }.iterator() }

    children.forEach { childWindow ->
        key(childWindow.uniqueKey)
        {
            childWindow.composable()
        }
    }

    return object:ChildWindowManager
    {
        override fun addChildWindow(createChildWindow:@Composable (ChildWindowManagerController)->Unit)
        {
            var function = ChildWindow(0) {}
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
            function = ChildWindow(uniqueKeyGenerator.next()) { createChildWindow(childWindowManagerRemote) }
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
