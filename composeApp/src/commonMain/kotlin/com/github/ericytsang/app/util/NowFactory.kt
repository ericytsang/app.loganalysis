package com.github.ericytsang.app.util

import java.time.Instant

interface NowFactory
{
    fun now():Instant

    companion object
    {
        val instance:NowFactory get() = NowFactoryImpl
    }
}

internal object NowFactoryImpl:NowFactory
{
    override fun now():Instant = Instant.now()
}