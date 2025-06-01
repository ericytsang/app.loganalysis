package org.example.project

import com.github.ericytsang.app.ui.frame.app.AppCommand
import com.github.ericytsang.kotlin.ImmutableCoroutineScope
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.consumeAsFlow

class MainOpenWindowsViewModel(
    uiScope:ImmutableCoroutineScope,
    appCommandChannel:ReceiveChannel<AppCommand>,
    handleCommand: (AppCommand) -> Unit,
    kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.Companion.instance,
):KotlinDependencyProvider by kotlinDependencyProvider
{
    private val appCommandChannelFlow = appCommandChannel.consumeAsFlow()

    init
    {
        // handle app commands in the background
        uiScope.launch(dispatchers.io)
        {
            appCommandChannelFlow.collect { command ->
                // invoke the provided command handler
                println("Handling command: $command")
                handleCommand(command)
            }
        }
    }
}