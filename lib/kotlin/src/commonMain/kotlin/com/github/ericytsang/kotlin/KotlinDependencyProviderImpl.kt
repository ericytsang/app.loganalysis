package com.github.ericytsang.kotlin

class KotlinDependencyProviderImpl(
    override val dispatchers:CoroutineDispatcherProvider = CoroutineDispatcherProviderImpl(),
):KotlinDependencyProvider