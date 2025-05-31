package com.github.ericytsang.app.ui.frame.workingfileseteditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.ericytsang.app.ui.modal.SettingsDialog
import kotlin.collections.minus
import kotlin.collections.plus

@Composable
fun childWindowManager(
    uniqueKeyPrefix:String,
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
                    children-function
                }
            }
            function = { createChildWindow(childWindowManagerRemote) }
            children+function
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
