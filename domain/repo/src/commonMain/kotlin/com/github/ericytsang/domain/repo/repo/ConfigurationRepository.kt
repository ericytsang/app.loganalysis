package com.github.ericytsang.domain.repo.repo

import app.cash.sqldelight.coroutines.asFlow
import com.github.ericytsang.domain.objects.Configuration
import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.domain.objects.ConfigurationName
import com.github.ericytsang.domain.objects.ConfigurationUpdateSequence
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.service.sqlite.ProjectEntity
import com.github.ericytsang.service.sqlite.dbfactory.DatabaseService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

interface ConfigurationRepository
{

    /**
     * Get an iterator over all active [Configuration]s.
     * Active configurations are those that have not been closed before last app exit.
     */
    fun getActiveProjects():SuspendIterator<Configuration>

    /**
     * Get a flow that emits whenever the configurations are changed.
     * This is useful for UI components that need to update when configurations change.
     */
    fun getOnChangedFlow():Flow<Unit>

    /**
     * Load the next N [Project]s that are before the given sequence number.
     * The [com.github.ericytsang.service.sqlite.Configuration]s are ordered by sequence number in descending order.
     */
    suspend fun loadNextNConfigurationIdsBefore(n:Int,sequenceNumber:ConfigurationUpdateSequence):List<Configuration>

    /**
     * Load the next N configuration IDs that are after the given sequence number.
     * The Configuration IDs are ordered by sequence number in ascending order.
     */
    suspend fun loadNextNConfigurationIdsAfter(n:Int,sequenceNumber:ConfigurationUpdateSequence):List<Configuration>

    /**
     * Delete the configuration with the given [configurationId].
     * This will also delete all the files associated with that configuration.
     */
    suspend fun deleteConfiguration(configurationId:ConfigurationId)
}

interface SuspendIterator<T>
{
    suspend fun next():T
    suspend fun hasNext():Boolean

    companion object
    {
        fun <T> Iterator<T>.asSuspendIterator(
            coroutineDispatcher:CoroutineDispatcher,
        ):SuspendIterator<T> = create(
            iterator = this,
            coroutineDispatcher = coroutineDispatcher,
        )

        private fun <T> create(
            iterator:Iterator<T>,
            coroutineDispatcher:CoroutineDispatcher,
        ):SuspendIterator<T> = object:SuspendIterator<T>
        {
            override suspend fun next():T = withContext(coroutineDispatcher) { iterator.next() }
            override suspend fun hasNext():Boolean = withContext(coroutineDispatcher) { iterator.hasNext() }
        }

        fun <T> Iterator<T>.asSuspendIterator():SuspendIterator<T> = create(iterator = this)

        private fun <T> create(
            iterator:Iterator<T>,
        ):SuspendIterator<T> = object:SuspendIterator<T>
        {
            override suspend fun next():T = iterator.next()
            override suspend fun hasNext():Boolean = iterator.hasNext()
        }

        fun <T,N> create(
            getNextQueryStartPoint:(List<T>)->N,
            query:suspend (N)->List<T>,
        ):SuspendIterator<T>
        {
            val projectSequence = generateSequence(emptyList<T>())
            { previousQueryResult ->
                val startingPoint = getNextQueryStartPoint(previousQueryResult)
                val queryResult = runBlocking { query(startingPoint) }
                if (queryResult.isEmpty())
                {
                    null // end of sequence
                }
                else
                {
                    queryResult
                }
            }
            return projectSequence
                .flatten()
                .iterator()
                .asSuspendIterator()
        }
    }
}

internal class ConfigurationRepositoryImpl(
    kotlinDependencyProvider:KotlinDependencyProvider,
    databaseService:DatabaseService,
):ConfigurationRepository,
    KotlinDependencyProvider by kotlinDependencyProvider,
    DatabaseService by databaseService
{
    override fun getActiveProjects():SuspendIterator<Configuration> = SuspendIterator.create(
        getNextQueryStartPoint = { previous ->
            previous
                .map { it.updateSequence }
                .plus(ConfigurationUpdateSequence.max)
                .minBy { it.updateSequence }
        },
        query = { startingPoint ->
            withContext(dispatchers.io)
            {
                queries.selectNextNActiveProjects(
                    update_sequence = startingPoint.updateSequence,
                    value_ = 10,// pageSize
                ).executeAsList().map { it.toDomainObject() }
            }
        },
    )

    override fun getOnChangedFlow():Flow<Unit> = queries.selectAllProjects().asFlow().map { }

    override suspend fun loadNextNConfigurationIdsBefore(
        n:Int,
        sequenceNumber:ConfigurationUpdateSequence,
    ):List<Configuration> = withContext(dispatchers.io)
    {
        queries.selectNProjectsUpdatedBefore(
            update_sequence = sequenceNumber.updateSequence,
            value_ = n.toLong(),
        ).executeAsList().map { it.toDomainObject() }
    }

    override suspend fun loadNextNConfigurationIdsAfter(
        n:Int,
        sequenceNumber:ConfigurationUpdateSequence,
    ):List<Configuration> = withContext(dispatchers.io)
    {
        queries.selectNProjectsUpdatedAfter(
            update_sequence = sequenceNumber.updateSequence,
            value_ = n.toLong(),
        ).executeAsList().map { it.toDomainObject() }.asReversed()
    }

    override suspend fun deleteConfiguration(
        configurationId:ConfigurationId,
    ) = withContext<Unit>(dispatchers.io)
    {
        transaction()
        {
            queries.deleteProject(configurationId.id)
        }
    }

    companion object
    {
        fun ProjectEntity.toDomainObject():Configuration
        {
            return Configuration(
                id = ConfigurationId(id),
                name = ConfigurationName(name),
                updateSequence = ConfigurationUpdateSequence(update_sequence),
                logcatFilter = logcat_filter,
            )
        }
    }
}