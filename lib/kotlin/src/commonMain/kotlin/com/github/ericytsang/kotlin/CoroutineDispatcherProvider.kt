package com.github.ericytsang.kotlin

import kotlinx.coroutines.CoroutineDispatcher

interface CoroutineDispatcherProvider
{
    val main:CoroutineDispatcher
    val default:CoroutineDispatcher
    val io:CoroutineDispatcher
}