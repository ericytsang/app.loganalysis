package com.github.ericytsang.service.sqlite.dependencyinjection

import com.github.ericytsang.service.sqlite.dbfactory.DatabaseFactory

interface SqliteDependencyProvider
{
    val databaseFactory:DatabaseFactory
}

object SqliteDependencyProviderImpl:SqliteDependencyProvider
{
    override val databaseFactory:DatabaseFactory by lazy { DatabaseFactory() }
}
