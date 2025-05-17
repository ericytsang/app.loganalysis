package com.github.ericytsang.kotlin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface CoroutineDispatcherProvider
{
    val main:CoroutineDispatcher
    val default:CoroutineDispatcher
    val io:CoroutineDispatcher
}

class CoroutineDispatcherProviderImpl:CoroutineDispatcherProvider
{
    override val main:CoroutineDispatcher get() = Dispatchers.Main
    override val default:CoroutineDispatcher get() = Dispatchers.Default
    override val io:CoroutineDispatcher get() = Dispatchers.IO
}
