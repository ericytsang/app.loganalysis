package com.github.ericytsang.kotlin

import kotlinx.coroutines.CoroutineScope

interface KotlinDependencyProvider
{
    val dispatchers:CoroutineDispatcherProvider
    val applicationScope:CoroutineScope
}

object KotlinDependencyProviderImpl:KotlinDependencyProvider
{
    override val dispatchers:CoroutineDispatcherProvider = CoroutineDispatcherProviderImpl()
    override val applicationScope:CoroutineScope = CoroutineScope(dispatchers.main)
}
