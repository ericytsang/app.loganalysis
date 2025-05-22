package com.github.ericytsang.domain.repo

import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.kotlin.KotlinDependencyProviderImpl
import com.github.ericytsang.service.sqlite.dependencyinjection.SqliteDependencyProvider
import com.github.ericytsang.service.sqlite.dependencyinjection.SqliteDependencyProviderImpl

interface RepositoryDependencyProvider
{
    val settingsRepository:SettingsRepository
    val workingFileSetRepository:WorkingFileSetRepository

    companion object
    {
        val instance:RepositoryDependencyProvider by lazy { RepositoryDependencyProviderImpl() }
    }
}

internal class RepositoryDependencyProviderImpl(
    private val kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProviderImpl,
    private val sqliteDependencyProvider:SqliteDependencyProvider = SqliteDependencyProviderImpl,
    private val appInfoService:AppInfoService = AppInfoServiceImpl()
):RepositoryDependencyProvider
{
    override val settingsRepository:SettingsRepository by lazy { SettingsRepositoryImpl() }

    override val workingFileSetRepository:WorkingFileSetRepository by lazy {
        WorkingFileSetRepositoryImpl(
            kotlinDependencyProvider = kotlinDependencyProvider,
            sqliteDependencyProvider = sqliteDependencyProvider,
            appInfoService = appInfoService,
        )
    }
}
