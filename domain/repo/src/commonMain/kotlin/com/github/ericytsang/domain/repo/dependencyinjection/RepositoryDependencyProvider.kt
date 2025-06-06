package com.github.ericytsang.domain.repo.dependencyinjection

import app.cash.sqldelight.coroutines.asFlow
import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.domain.objects.FilterInterpretationMode
import com.github.ericytsang.domain.objects.FilterModel
import com.github.ericytsang.domain.objects.FilterModelId
import com.github.ericytsang.domain.objects.FilterType
import com.github.ericytsang.domain.objects.OrderIndex
import com.github.ericytsang.domain.repo.repo.ConfigurationRepository
import com.github.ericytsang.domain.repo.repo.ConfigurationRepositoryImpl
import com.github.ericytsang.domain.repo.repo.DelimiterRepository
import com.github.ericytsang.domain.repo.repo.DelimiterRepositoryImpl
import com.github.ericytsang.domain.repo.repo.FilterRepository
import com.github.ericytsang.domain.repo.repo.FilterRepositoryImpl
import com.github.ericytsang.domain.repo.repo.SettingsRepository
import com.github.ericytsang.domain.repo.repo.SettingsRepositoryImpl
import com.github.ericytsang.domain.repo.repo.ThemeRepository
import com.github.ericytsang.domain.repo.repo.ThemeRepositoryImpl
import com.github.ericytsang.domain.repo.repo.WorkingFileSetRepository
import com.github.ericytsang.domain.repo.repo.WorkingFileSetRepositoryImpl
import com.github.ericytsang.domain.repo.service.DatabaseServiceProvider
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.service.sqlite.FilterEntity
import com.github.ericytsang.service.sqlite.dbfactory.DatabaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface RepositoryDependencyProvider
{
    val settingsRepository:SettingsRepository
    val themeRepository:ThemeRepository
    val delimiterRepository:DelimiterRepository
    val workingFileSetRepository:WorkingFileSetRepository
    val configurationRepository:ConfigurationRepository
    val filterRepository:FilterRepository

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

    override val configurationRepository:ConfigurationRepository by lazy {
        ConfigurationRepositoryImpl(
            kotlinDependencyProvider = kotlinDependencyProvider,
            databaseService = databaseServiceProvider.databaseService,
        )
    }

    override val filterRepository:FilterRepository by lazy {
        FilterRepositoryImpl(
            kotlinDependencyProvider = kotlinDependencyProvider,
            databaseService = databaseServiceProvider.databaseService,
        )
    }
}