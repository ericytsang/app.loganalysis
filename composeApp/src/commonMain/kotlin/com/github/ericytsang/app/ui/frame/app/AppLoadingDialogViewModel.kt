package com.github.ericytsang.app.ui.frame.app

import com.github.ericytsang.kotlin.ImmutableCoroutineScope
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow

class AppLoadingDialogViewModel(
    uiScope:ImmutableCoroutineScope,
    loadingFinishedSignalChannel:ReceiveChannel<Unit>,
    kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
):KotlinDependencyProvider by kotlinDependencyProvider
{
    private val _shouldShowLoadingDialogFlow = MutableStateFlow(true)
    val shouldShowLoadingDialogFlow:Flow<Boolean> get() = _shouldShowLoadingDialogFlow

    init
    {
        // hide the loading dialog after a loading finished signal is received
        uiScope.launch(dispatchers.io)
        {
            loadingFinishedSignalChannel.receiveAsFlow().first()
            _shouldShowLoadingDialogFlow.value = false
        }
    }
}