package com.github.ericytsang.app.ui.frame.workingfileseteditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.ericytsang.app.ui.frame.logviewer.LogViewerRootWindow
import com.github.ericytsang.app.ui.frame.newprojectwizard.NewProjectWizardWindow
import com.github.ericytsang.app.ui.frame.projectbrowser.ProjectBrowserWindow
import com.github.ericytsang.app.ui.modal.SettingsWindow
import com.github.ericytsang.domain.objects.ConfigurationId
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

data class ChildWindow(
    val uniqueKey:Any,
    val composable:@Composable () -> Unit,
    val childWindowManagerController:MutableChildWindowManagerController,
)

@Composable
fun childWindowManager(
    onFinalWindowClosed:() -> Unit = {},
):ChildWindowManager
{
    var children by remember { mutableStateOf<Map<Any,ChildWindow>>(emptyMap()) }

    // render all existing child windows
    children.values.forEach()
    { childWindow ->
        key(childWindow.uniqueKey)
        {
            childWindow.composable()
        }
    }

    return object:ChildWindowManager
    {
        override fun addChildWindow(
            key:Any,
            createChildWindow:@Composable (ChildWindowManagerController)->Unit,
        )
        {
            if (key in children.keys)
            {
                // if a child window with the same key already exists, bring it to the front
                bringExistingChildWindowToFront(key)
            }
            else
            {
                // otherwise, create a new child window
                addNewChildWindow(key, createChildWindow)
            }
        }

        private fun bringExistingChildWindowToFront(key:Any)
        {
            val childWindowManagerController = children[key] ?: return
            val commandChannel = childWindowManagerController.childWindowManagerController.commands
            commandChannel.trySend(ChildWindowCommand.BringToFocus)
        }

        private fun addNewChildWindow(
            key:Any,
            createChildWindow:@Composable (ChildWindowManagerController)->Unit,
        )
        {
            var function:ChildWindow? = null
            val childWindowManagerRemote = object:MutableChildWindowManagerController
            {
                override fun removeSelf()
                {
                    children -= key
                    if (children.isEmpty())
                    {
                        onFinalWindowClosed()
                    }
                }

                override val commands:Channel<ChildWindowCommand> = Channel(Channel.CONFLATED)
            }
            function = ChildWindow(
                uniqueKey = key,
                composable = { createChildWindow(childWindowManagerRemote) },
                childWindowManagerController = childWindowManagerRemote,
            )
            children += key to function
        }
    }
}

interface MutableChildWindowManagerController:ChildWindowManagerController
{
    override val commands:Channel<ChildWindowCommand>
}

/**
 * This interface is used to allow child windows to remove themselves from the ChildWindowManager.
 * It is used to avoid having to pass the ChildWindowManager instance to the child window.
 */
interface ChildWindowManagerController
{
    fun removeSelf()
    val commands:ReceiveChannel<ChildWindowCommand>
}

sealed class ChildWindowCommand
{
    data object BringToFocus:ChildWindowCommand()
}

interface ChildWindowManager
{
    fun addChildWindow(

        /**
         * A unique key for the child window.
         * This is used to ensure that the child window is only created once.
         * If another request to open a child window with the same key is made,
         * then the existing child window will be brought to the front instead of creating a new one.
         */
        key:Any = Any(),

        /**
         * Creates a child window.
         * The [ChildWindowManagerController] is used to remove the child window when it is closed.
         */
        createChildWindow:@Composable (ChildWindowManagerController)->Unit,
    )
}

fun ChildWindowManager.openSettingsDialog()
{
    addChildWindow("openSettingsDialog") { controller -> SettingsWindow(controller) }
}

fun ChildWindowManager.openLogViewer(configurationId:ConfigurationId)
{
    addChildWindow(configurationId) { controller ->
        LogViewerRootWindow(
            configurationId = configurationId,
            rootChildWindowManager = this,
            controller = controller,
        )
    }
}

fun ChildWindowManager.openNewProjectWizard()
{
    addChildWindow("openNewProjectWizard") { controller -> NewProjectWizardWindow(this, controller) }
}

fun ChildWindowManager.openProjectBrowser()
{
    addChildWindow("openProjectBrowser") { controller -> ProjectBrowserWindow(this, controller) }
}
