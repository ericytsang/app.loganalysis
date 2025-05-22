package com.github.ericytsang.app.app

import java.time.Instant

interface NowFactory
{
    fun now():Instant
}

object NowFactoryImpl:NowFactory
{
    override fun now():Instant = Instant.now()
}