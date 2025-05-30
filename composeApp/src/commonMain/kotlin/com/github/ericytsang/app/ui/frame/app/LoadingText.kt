package com.github.ericytsang.app.ui.frame.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.kotlin.ImmutableCoroutineScope
import com.github.ericytsang.kotlin.ImmutableCoroutineScope.Companion.asImmutableCoroutineScope
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun LoadingText(
    viewModelFactory: (ImmutableCoroutineScope) -> LoadingTextViewModel = { coroutineScope -> LoadingTextViewModel(coroutineScope) }
)
{
    val coroutineScope = rememberCoroutineScope()
    val viewModel = remember { viewModelFactory(coroutineScope.asImmutableCoroutineScope()) }
    val loadingText by viewModel.loadingText.collectAsState("")
    Text(
        text = loadingText,
        modifier = Modifier.padding(Dimens.mttPadding),
    )
}

class LoadingTextViewModel(
    uiScope: ImmutableCoroutineScope,
    kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.Companion.instance
):KotlinDependencyProvider by kotlinDependencyProvider
{
    private val animationFrameFlow = MutableStateFlow(0)
    private var animationFrame: Int by animationFrameFlow::value

    init
    {
        uiScope.launch()
        {
            while (isActive)
            {
                delay(500)
                animationFrame++
            }
        }
    }

    val loadingText = animationFrameFlow.map { frame ->
        val dots = (0 until (frame%4)).joinToString("") { "." }
        "Loading${dots}"
    }
}
