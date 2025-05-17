package com.github.ericytsang.kotlin

interface KotlinDependencyProvider
{
    val dispatchers:CoroutineDispatcherProvider
}

object KotlinDependencyProviderImpl:KotlinDependencyProvider
{
    override val dispatchers:CoroutineDispatcherProvider = CoroutineDispatcherProviderImpl()
}
