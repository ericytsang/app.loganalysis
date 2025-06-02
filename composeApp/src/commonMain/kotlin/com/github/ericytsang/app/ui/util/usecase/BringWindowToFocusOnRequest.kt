package com.github.ericytsang.app.ui.util.usecase

import androidx.compose.ui.awt.ComposeWindow
import com.github.ericytsang.app.ui.util.ChildWindowCommand
import com.github.ericytsang.app.ui.util.ChildWindowManagerController
import com.github.ericytsang.kotlin.ImmutableCoroutineScope
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.flow.receiveAsFlow

class BringWindowToFocusOnRequest(
    uiScope:ImmutableCoroutineScope,
    private val window:ComposeWindow,
    private val controller:ChildWindowManagerController,
    private val kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.Companion.instance,
) : KotlinDependencyProvider by kotlinDependencyProvider
{
    init
    {
        uiScope.launch(dispatchers.io)
        {
            controller.commands.receiveAsFlow().collect()
            { command ->
                when (command)
                {
                    ChildWindowCommand.BringToFocus -> window.requestFocus()
                }
            }
        }
    }
}