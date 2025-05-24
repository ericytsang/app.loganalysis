package com.github.ericytsang.domain.repo.repo

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.service.sqlite.dbfactory.DatabaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface SettingsRepository
{
    fun getShouldWrapTextFlow():Flow<Boolean>
    suspend fun setShouldWrapText(shouldWrapText:Boolean)
}

internal class SettingsRepositoryImpl(
    private val kotlinDependencyProvider:KotlinDependencyProvider,
    private val databaseService:DatabaseService,
):SettingsRepository,
    KotlinDependencyProvider by kotlinDependencyProvider
{
    override fun getShouldWrapTextFlow():Flow<Boolean> = databaseService.queries.selectSettings().asFlow()
        .mapToList(dispatchers.io)
        .map { entities -> entities.firstOrNull() }
        .map { settingsEntity -> settingsEntity?.should_wrap_text != 0L }
        .flowOn(dispatchers.io)

    override suspend fun setShouldWrapText(shouldWrapText:Boolean) = withContext(dispatchers.io)
    {
        databaseService.transaction<Unit>()
        {
            databaseService.queries.updateShouldWrapText(if (shouldWrapText) 1L else 0L)
        }
    }
}
