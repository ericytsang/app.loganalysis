package com.github.ericytsang.app.ui.util.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.github.ericytsang.kotlin.ImmutableCoroutineScope
import com.github.ericytsang.kotlin.ImmutableCoroutineScope.Companion.asImmutableCoroutineScope
import com.github.ericytsang.kotlin.newChildScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun DoubleClickButton(
    modifier:Modifier = Modifier,
    onDoubleClick:()->Unit,
    idleButtonColors:ButtonColors,
    idleContent:@Composable RowScope.()->Unit,
    clickedOnceButtonColors:ButtonColors,
    clickedOnceContent:@Composable RowScope.()->Unit,
)
{
    val uiScope = rememberCoroutineScope()
    val viewModel = remember {
        ClickAgainToConfirmButtonViewModel(
            uiScope = uiScope.asImmutableCoroutineScope(),
            onDoubleClick = onDoubleClick,
        )
    }
    val buttonState by viewModel.state.collectAsState(ClickAgainToConfirmButtonViewModel.State.Idle {})
    AnimatedContent(buttonState)
    { state ->
        when (state)
        {
            is ClickAgainToConfirmButtonViewModel.State.Idle -> Button(
                modifier = modifier,
                onClick = viewModel::onClick,
                colors = idleButtonColors,
                content = idleContent,
            )

            is ClickAgainToConfirmButtonViewModel.State.ClickedOnce -> Button(
                modifier = modifier,
                onClick = viewModel::onClick,
                colors = clickedOnceButtonColors,
                content = clickedOnceContent,
            )
        }
    }
}

private class ClickAgainToConfirmButtonViewModel(
    val uiScope:ImmutableCoroutineScope,
    val onDoubleClick:()->Unit,
    val doubleClickTimeout:Duration = 3.seconds,
)
{
    fun onClick()
    {
        _state.value.onClick()
    }

    private val requestMoveToClickedOnceState = object:()->Unit
    {
        override fun invoke()
        {
            setState(
                newState = State.ClickedOnce(
                    uiScope = uiScope,
                    doubleClickTimeout = doubleClickTimeout,
                    onDoubleClickDetected = onDoubleClick,
                    requestReturnToIdleState = { setState(State.Idle(this)) },
                )
            )
        }
    }

    private val _state = MutableStateFlow<State>(State.Idle(requestMoveToClickedOnceState))

    val state:Flow<State> get() = _state

    private fun setState(newState:State)
    {
        _state.value.onLeaveState()
        _state.value = newState
    }

    sealed class State
    {
        abstract fun onClick()

        open fun onLeaveState() = Unit

        data class Idle(
            val requestMoveToClickedOnceState:()->Unit,
        ):State()
        {
            override fun onClick()
            {
                requestMoveToClickedOnceState()
            }
        }

        data class ClickedOnce(
            val uiScope:ImmutableCoroutineScope,
            val doubleClickTimeout:Duration,
            val onDoubleClickDetected:()->Unit,
            val requestReturnToIdleState:()->Unit,
        ):State()
        {
            private val stateScope = uiScope.newChildScope()

            init
            {
                stateScope.launch {
                    // wait for double click timeout
                    delay(doubleClickTimeout)
                    // reset state to Idle after timeout
                    requestReturnToIdleState()
                }
            }

            override fun onClick()
            {
                onDoubleClickDetected()
                requestReturnToIdleState()
            }

            override fun onLeaveState()
            {
                stateScope.cancel()
            }
        }
    }
}