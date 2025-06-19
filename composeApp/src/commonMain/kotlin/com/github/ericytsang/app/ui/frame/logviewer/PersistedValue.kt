package com.github.ericytsang.app.ui.frame.logviewer

import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.kotlin.launchIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class PersistedValue<T>(
    initialValue:T,
    updatePersistedValue: suspend (T)->Unit,
    kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
):KotlinDependencyProvider by kotlinDependencyProvider
{
    private sealed class Sourced<T>
    {
        abstract val value:T
        data class User<T>(override val value:T):Sourced<T>()
        data class System<T>(override val value:T):Sourced<T>()
    }

    private val _valueFlow = MutableStateFlow<Sourced<T>>(Sourced.System(initialValue))
    val valueFlow:Flow<T> get() = _valueFlow.map { it.value }

    init
    {
        _valueFlow
            .filter { it is Sourced.User }
            .conflate()
            .map { it.value }
            .onEach { newValue -> updatePersistedValue(newValue) }
            .flowOn(dispatchers.io)
            .launchIn(applicationScope)
    }

    var value:T
        get() = _valueFlow.value.value
        set(newValue) { _valueFlow.value = Sourced.User(newValue) }
}