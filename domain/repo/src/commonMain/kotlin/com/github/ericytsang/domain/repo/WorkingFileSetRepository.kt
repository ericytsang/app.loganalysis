package com.github.ericytsang.domain.repo

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.domain.objects.LogFile
import com.github.ericytsang.domain.objects.OrderIndex
import com.github.ericytsang.domain.objects.WorkingFileSet
import com.github.ericytsang.domain.objects.WorkingFileSetSelected
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.service.sqlite.LogFiles
import com.github.ericytsang.service.sqlite.dependencyinjection.SqliteDependencyProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

interface WorkingFileSetRepository
{
    suspend fun createNewWorkingFileSet(newFiles:List<File>):ConfigurationId

    fun getWorkingFileSetFlow(configurationId:ConfigurationId):Flow<WorkingFileSet>
    suspend fun addFiles(configurationId:ConfigurationId,newFiles:List<File>)
    suspend fun moveFilesToPosition(configurationId:ConfigurationId,files:List<File>,position:Int)
    suspend fun removeFiles(configurationId:ConfigurationId,files:List<File>)
}

class WorkingFileSetRepositoryImpl(
    private val kotlinDependencyProvider:KotlinDependencyProvider,
    private val sqliteDependencyProvider:SqliteDependencyProvider,
    private val appInfoService:AppInfoService,
):WorkingFileSetRepository,
    KotlinDependencyProvider by kotlinDependencyProvider,
    SqliteDependencyProvider by sqliteDependencyProvider
{
    private val database by lazy { databaseFactory.getDatabase(appInfoService.getAppPackageName()) }

    private val queries get() = database.logAnalysisQueries

    override suspend fun createNewWorkingFileSet(
        newFiles:List<File>,
    ):ConfigurationId = withContext(dispatchers.io)
    {
        database.transactionWithResult {
            queries.insertConfiguration("").value
            val newConfigurationId = queries.lastInsertId().executeAsOne()
            newFiles.forEachIndexed { index,file ->
                queries.insertLogFile(
                    config_id = newConfigurationId,
                    file_path = file.absolutePath,
                    order_index = index.toLong(),
                )
            }
            ConfigurationId(newConfigurationId)
        }
    }

    override fun getWorkingFileSetFlow(
        configurationId:ConfigurationId,
    ):Flow<WorkingFileSet> = queries.selectLogFilesForConfig(configurationId.id).asFlow()
        .mapToList(dispatchers.io)
        .map { logFiles ->
            WorkingFileSetSelected(
                configurationId = configurationId,
                files = logFiles.map { logFile -> parseLogFileToDomainObject(logFile) },
            )
        }
        .flowOn(dispatchers.io)

    override suspend fun addFiles(
        configurationId:ConfigurationId,
        newFiles:List<File>,
    ) = withContext(dispatchers.io)
    {
        database.transaction {
            val logFilesForConfiguration = queries
                .selectLogFilesForConfig(configurationId.id)
                .executeAsList()
            val maxOrderIndex = logFilesForConfiguration
                .maxOfOrNull { it.order_index } ?: 0L
            val firstOrderIndexToUse = maxOrderIndex+1L
            newFiles.forEachIndexed { index,file ->
                queries.insertLogFile(
                    config_id = configurationId.id,
                    file_path = file.absolutePath,
                    order_index = index.toLong()+firstOrderIndexToUse,
                )
            }
        }
    }

    override suspend fun moveFilesToPosition(
        configurationId:ConfigurationId,
        files:List<File>,
        position:Int,
    )
    {
        TODO("Not yet implemented")
    }

    override suspend fun removeFiles(
        configurationId:ConfigurationId,
        files:List<File>,
    )
    {
        TODO("Not yet implemented")
    }

    companion object
    {
        private fun parseLogFileToDomainObject(
            logFile:LogFiles,
        ):LogFile = LogFile(
            filePath = logFile.file_path,
            orderIndex = OrderIndex(logFile.order_index),
        )
    }
}