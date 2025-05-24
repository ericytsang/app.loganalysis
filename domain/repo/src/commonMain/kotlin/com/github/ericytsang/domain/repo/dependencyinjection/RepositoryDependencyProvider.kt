package com.github.ericytsang.domain.repo.dependencyinjection

import com.github.ericytsang.domain.repo.repo.DelimiterRepository
import com.github.ericytsang.domain.repo.repo.DelimiterRepositoryImpl
import com.github.ericytsang.domain.repo.repo.SettingsRepository
import com.github.ericytsang.domain.repo.repo.SettingsRepositoryImpl
import com.github.ericytsang.domain.repo.repo.ThemeRepository
import com.github.ericytsang.domain.repo.repo.ThemeRepositoryImpl
import com.github.ericytsang.domain.repo.repo.WorkingFileSetRepository
import com.github.ericytsang.domain.repo.repo.WorkingFileSetRepositoryImpl
import com.github.ericytsang.domain.repo.service.DatabaseServiceProvider
import com.github.ericytsang.kotlin.KotlinDependencyProvider

interface RepositoryDependencyProvider
{
    val settingsRepository:SettingsRepository
    val themeRepository:ThemeRepository
    val delimiterRepository:DelimiterRepository
    val workingFileSetRepository:WorkingFileSetRepository

    companion object
    {
        val instance:RepositoryDependencyProvider by lazy { RepositoryDependencyProviderImpl() }
    }
}

internal class RepositoryDependencyProviderImpl(
    private val kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
    private val databaseServiceProvider:DatabaseServiceProvider = DatabaseServiceProvider.instance,
):RepositoryDependencyProvider
{
    override val settingsRepository:SettingsRepository by lazy {
        SettingsRepositoryImpl(
            kotlinDependencyProvider = kotlinDependencyProvider,
            databaseService = databaseServiceProvider.databaseService,
        )
    }

    override val themeRepository:ThemeRepository by lazy {
        ThemeRepositoryImpl(
            kotlinDependencyProvider = kotlinDependencyProvider,
            databaseService = databaseServiceProvider.databaseService,
        )
    }

    override val delimiterRepository:DelimiterRepository by lazy {
        DelimiterRepositoryImpl(
            kotlinDependencyProvider = kotlinDependencyProvider,
            databaseService = databaseServiceProvider.databaseService,
        )
    }

    override val workingFileSetRepository:WorkingFileSetRepository by lazy {
        WorkingFileSetRepositoryImpl(
            kotlinDependencyProvider = kotlinDependencyProvider,
            databaseService = databaseServiceProvider.databaseService,
        )
    }
}
