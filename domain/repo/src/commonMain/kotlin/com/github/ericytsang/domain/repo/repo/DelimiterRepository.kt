package com.github.ericytsang.domain.repo.repo

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.service.sqlite.dbfactory.DatabaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface DelimiterRepository
{
    fun getDelimiterFlow():Flow<String>
    suspend fun setDelimiter(newDelimiters: String)
}

internal class DelimiterRepositoryImpl(
    private val kotlinDependencyProvider:KotlinDependencyProvider,
    private val databaseService:DatabaseService,
):DelimiterRepository,
    KotlinDependencyProvider by kotlinDependencyProvider
{
    override fun getDelimiterFlow():Flow<String> = databaseService.queries.selectSettings().asFlow()
        .mapToList(dispatchers.io)
        .map { entities -> entities.firstOrNull() }
        .map { settingsEntity -> settingsEntity?.color_coding_delimiters ?: "" }
        .flowOn(dispatchers.io)

    override suspend fun setDelimiter(newDelimiters: String) = withContext<Unit>(dispatchers.io)
    {
        databaseService.transaction()
        {
            databaseService.queries.updateColorCodingDelimiters(newDelimiters)
        }
    }
}
