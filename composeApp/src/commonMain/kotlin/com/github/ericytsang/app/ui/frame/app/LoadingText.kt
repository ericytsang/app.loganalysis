package com.github.ericytsang.app.ui.frame.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoadingText(kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.Companion.instance)
{
    var animationFrame by mutableStateOf(0)
    kotlinDependencyProvider.applicationScope.launch {
        delay(500)
        animationFrame++
    }
    val dots = (0 until (animationFrame % 4)).joinToString("") { "." }
    Text(
        text = "Loading${dots}",
        modifier = Modifier.Companion.padding(Dimens.mttPadding),
    )
}