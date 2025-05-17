package com.github.ericytsang.service.sqlite

interface SqliteDependencyProvider
{
    val databaseFactory:DatabaseFactory
}

object SqliteDependencyProviderImpl:SqliteDependencyProvider
{
    override val databaseFactory:DatabaseFactory by lazy { DatabaseFactory() }
}
