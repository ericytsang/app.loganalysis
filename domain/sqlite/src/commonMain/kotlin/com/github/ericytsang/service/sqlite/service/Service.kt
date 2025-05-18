package com.github.ericytsang.service.sqlite.service

import app.cash.sqldelight.db.QueryResult
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.service.sqlite.SqlDelightDatabase
import com.github.ericytsang.service.sqlite.dependencyinjection.SqliteDependencyProvider
import kotlinx.coroutines.withContext

interface SqliteService
{

}

class SqliteServiceImpl(
    kotlinDependencyProvider:KotlinDependencyProvider,
    getDatabase:()->SqlDelightDatabase,
):KotlinDependencyProvider by kotlinDependencyProvider
{
    private val database by lazy { getDatabase() }

    suspend fun insertConfiguration(
        configurationName:String = "",
    ):Long = withContext(dispatchers.io)
    {
        database.transactionWithResult(noEnclosing = true) {
            database.logAnalysisQueries.insertConfiguration(configurationName)
            database.logAnalysisQueries.lastInsertId().executeAsOne()
        }
    }
}