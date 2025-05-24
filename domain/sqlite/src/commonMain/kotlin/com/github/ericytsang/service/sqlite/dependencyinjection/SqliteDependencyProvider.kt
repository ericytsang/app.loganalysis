package com.github.ericytsang.service.sqlite.dependencyinjection

import com.github.ericytsang.service.sqlite.dbfactory.DatabaseFactory

interface SqliteDependencyProvider
{
    val databaseFactory:DatabaseFactory

    companion object
    {
        val instance:SqliteDependencyProvider by lazy { SqliteDependencyProviderImpl }
    }
}

internal object SqliteDependencyProviderImpl:SqliteDependencyProvider
{
    override val databaseFactory:DatabaseFactory by lazy { DatabaseFactory.createDefault() }
}
