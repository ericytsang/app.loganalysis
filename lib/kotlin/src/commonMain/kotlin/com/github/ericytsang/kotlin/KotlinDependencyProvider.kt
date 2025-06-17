package com.github.ericytsang.kotlin

import com.github.ericytsang.kotlin.ImmutableCoroutineScope.Companion.asImmutableCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.shareIn
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

fun <T> Flow<T>.launchIn(scope: ImmutableCoroutineScope): Job = scope.launch {
    collect() // tail-call
}

sealed class ImmutableCoroutineScope
{
    abstract fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend ImmutableCoroutineScope.() -> Unit,
    ): Job

    abstract fun <T> async(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend ImmutableCoroutineScope.() -> T,
    ): kotlinx.coroutines.Deferred<T>

    abstract val isActive:Boolean

    companion object
    {
        fun CoroutineScope.asImmutableCoroutineScope(): ImmutableCoroutineScope = ImmutableCoroutineScopeImpl(this)
    }
}

fun <T> Flow<T>.shareIn(
    scope: ImmutableCoroutineScope,
    started: SharingStarted,
    replay: Int = 0
): SharedFlow<T> = shareIn(
    scope = scope.getMutableScope(),
    started = started,
    replay = replay,
)

private fun ImmutableCoroutineScope.getMutableScope():CoroutineScope = when (this)
{
    is ImmutableCoroutineScopeImpl -> mutableCoroutineScope
}

private class ImmutableCoroutineScopeImpl(
    val mutableCoroutineScope: CoroutineScope
):ImmutableCoroutineScope()
{
    override fun launch(
        context: CoroutineContext,
        start: CoroutineStart,
        block: suspend ImmutableCoroutineScope.() -> Unit,
    ) = mutableCoroutineScope.launch(
        context = context,
        start = start,
        block = { block() },
    )

    override fun <T> async(
        context: CoroutineContext,
        start: CoroutineStart,
        block: suspend ImmutableCoroutineScope.() -> T,
    ) = mutableCoroutineScope.async(
        context = context,
        start = start,
        block = { block() },
    )

    override val isActive get() = mutableCoroutineScope.isActive
}
