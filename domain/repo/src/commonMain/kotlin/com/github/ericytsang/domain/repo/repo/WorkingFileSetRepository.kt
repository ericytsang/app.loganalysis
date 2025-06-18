package com.github.ericytsang.domain.repo.repo

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.domain.objects.FilePath
import com.github.ericytsang.domain.objects.LogFile
import com.github.ericytsang.domain.objects.OrderIndex
import com.github.ericytsang.domain.objects.WorkingFileSet
import com.github.ericytsang.domain.objects.WorkingFileSetSelected
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.service.sqlite.LogFileEntity
import com.github.ericytsang.service.sqlite.dbfactory.DatabaseService
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
    suspend fun moveFile(configurationId:ConfigurationId,from:FilePath,to:FilePath)
    suspend fun removeFile(configurationId:ConfigurationId,filePath:FilePath)
}

internal class WorkingFileSetRepositoryImpl(
    private val kotlinDependencyProvider:KotlinDependencyProvider,
    private val databaseService:DatabaseService,
):WorkingFileSetRepository,
    KotlinDependencyProvider by kotlinDependencyProvider
{
    private val database get() = databaseService

    private val queries get() = database.queries

    override suspend fun createNewWorkingFileSet(
        newFiles:List<File>,
    ):ConfigurationId = withContext(dispatchers.io)
    {
        database.transaction {
            val lastUpdated = queries.selectLastUpdatedProject().executeAsOneOrNull()
            val updateSequence = lastUpdated?.update_sequence?.plus(1) ?: 1L
            queries.insertProject("",updateSequence,"",0)
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

    override suspend fun moveFile(
        configurationId:ConfigurationId,
        from:FilePath,
        to:FilePath,
    ) = withContext<Unit>(dispatchers.io)
    {
        database.transaction()
        {
            // get the files to move from the database

            // move the "from" file being moved to a temporary order index

            // decide if we need to increment or decrement the order index of other items,
            // which depends on whether the item being is being moved to a higher or lower order index

            // increment or decrement the order index of other items

            // update the items being moved to move it to the destination
        }
    }

    override suspend fun removeFile(
        configurationId:ConfigurationId,
        filePath:FilePath,
    ) = withContext<Unit>(dispatchers.io)
    {
        database.transaction {
            queries.deleteLogFile(
                config_id = configurationId.id,
                file_path = filePath.filePath,
            )
        }
    }

    companion object
    {
        private fun parseLogFileToDomainObject(
            logFile:LogFileEntity,
        ):LogFile = LogFile(
            filePath = FilePath(logFile.file_path),
            orderIndex = OrderIndex(logFile.order_index),
        )
    }
}