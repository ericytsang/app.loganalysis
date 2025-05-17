package com.github.ericytsang.kotlin

object KotlinDependencyProviderImpl:KotlinDependencyProvider
{
    override val dispatchers:CoroutineDispatcherProvider = CoroutineDispatcherProviderImpl()
}