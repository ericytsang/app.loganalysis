package com.github.ericytsang.domain.repo.service

import com.github.ericytsang.domain.appinfo.AppInfoService
import com.github.ericytsang.service.sqlite.dbfactory.DatabaseService
import com.github.ericytsang.service.sqlite.dependencyinjection.SqliteDependencyProvider

internal interface DatabaseServiceProvider
{
    val databaseService:DatabaseService

    companion object
    {
        val instance:DatabaseServiceProvider by lazy {
            DatabaseServiceProviderImpl(
                sqliteDependencyProvider = SqliteDependencyProvider.instance,
                appInfoService = AppInfoService.instance,
            )
        }
    }
}

internal class DatabaseServiceProviderImpl(
    private val sqliteDependencyProvider:SqliteDependencyProvider,
    private val appInfoService:AppInfoService,
):DatabaseServiceProvider
{
    override val databaseService:DatabaseService get() =
        sqliteDependencyProvider.databaseFactory.getDatabase(appInfoService.getAppPackageName())
}
