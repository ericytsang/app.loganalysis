package com.github.ericytsang.kotlin

import kotlinx.coroutines.CoroutineScope

interface KotlinDependencyProvider
{
    val dispatchers:CoroutineDispatcherProvider
    val applicationScope:CoroutineScope

    companion object
    {
        val instance:KotlinDependencyProvider by lazy { KotlinDependencyProviderImpl }
    }
}

internal object KotlinDependencyProviderImpl:KotlinDependencyProvider
{
    override val dispatchers:CoroutineDispatcherProvider = CoroutineDispatcherProviderImpl()
    override val applicationScope:CoroutineScope = CoroutineScope(dispatchers.main)
}
