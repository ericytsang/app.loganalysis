package com.github.ericytsang.domain.repo.repo

import app.cash.sqldelight.coroutines.asFlow
import com.github.ericytsang.domain.objects.Configuration
import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.domain.objects.ConfigurationName
import com.github.ericytsang.domain.objects.ConfigurationUpdateSequence
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.service.sqlite.ConfigurationEntity
import com.github.ericytsang.service.sqlite.dbfactory.DatabaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface ConfigurationRepository
{
    /**
     * Get a flow that emits whenever the configurations are changed.
     * This is useful for UI components that need to update when configurations change.
     */
    fun getOnChangedFlow():Flow<Unit>

    /**
     * Load the next N [Configurations]s that are before the given sequence number.
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

internal class ConfigurationRepositoryImpl(
    kotlinDependencyProvider:KotlinDependencyProvider,
    databaseService:DatabaseService,
):ConfigurationRepository,
    KotlinDependencyProvider by kotlinDependencyProvider,
    DatabaseService by databaseService
{
    override fun getOnChangedFlow():Flow<Unit> = queries.selectAllConfigurations().asFlow().map { }

    override suspend fun loadNextNConfigurationIdsBefore(
        n:Int,
        sequenceNumber:ConfigurationUpdateSequence,
    ):List<Configuration> = withContext(dispatchers.io)
    {
        queries.selectNConfigurationsUpdatedBefore(
            update_sequence = sequenceNumber.updateSequence,
            value_ = n.toLong(),
        ).executeAsList().map { it.toDomainObject() }
    }

    override suspend fun loadNextNConfigurationIdsAfter(
        n:Int,
        sequenceNumber:ConfigurationUpdateSequence,
    ):List<Configuration> = withContext(dispatchers.io)
    {
        queries.selectNConfigurationsUpdatedAfter(
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
            queries.deleteConfiguration(configurationId.id)
        }
    }

    companion object
    {
        fun ConfigurationEntity.toDomainObject():Configuration
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