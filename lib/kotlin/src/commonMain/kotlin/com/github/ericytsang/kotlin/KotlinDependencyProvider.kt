package com.github.ericytsang.kotlin

import com.github.ericytsang.kotlin.ImmutableCoroutineScope.Companion.asImmutableCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

interface KotlinDependencyProvider
{
    val dispatchers:CoroutineDispatcherProvider
    val applicationScope:ImmutableCoroutineScope

    companion object
    {
        val instance:KotlinDependencyProvider by lazy { KotlinDependencyProviderImpl }
    }
}

internal object KotlinDependencyProviderImpl:KotlinDependencyProvider
{
    override val dispatchers:CoroutineDispatcherProvider = CoroutineDispatcherProviderImpl()
    override val applicationScope:ImmutableCoroutineScope = CoroutineScope(dispatchers.main).asImmutableCoroutineScope()
}

fun CoroutineScope.newChildScope(
    coroutineContext:CoroutineContext = EmptyCoroutineContext,
):CoroutineScope
{
    val job = launch(coroutineContext) { awaitCancellation() }
    return CoroutineScope(coroutineContext + job)
}

fun ImmutableCoroutineScope.newChildScope(
    coroutineContext:CoroutineContext = EmptyCoroutineContext,
):CoroutineScope
{
    val job = launch(coroutineContext) { awaitCancellation() }
    return CoroutineScope(coroutineContext + job)
}

class ImmutableCoroutineScope(
    private val mutableCoroutineScope: CoroutineScope
)
{
    fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend ImmutableCoroutineScope.() -> Unit,
    ) = mutableCoroutineScope.launch(
        context = context,
        start = start,
        block = { block() },
    )

    fun <T> async(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend ImmutableCoroutineScope.() -> T,
    ) = mutableCoroutineScope.async(
        context = context,
        start = start,
        block = { block() },
    )

    val isActive get() = mutableCoroutineScope.isActive

    companion object
    {
        fun CoroutineScope.asImmutableCoroutineScope(): ImmutableCoroutineScope = ImmutableCoroutineScope(this)
    }
}
