package com.github.ericytsang.domain.repo.repo

import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.service.sqlite.Configuration
import com.github.ericytsang.service.sqlite.dbfactory.DatabaseService
import kotlinx.coroutines.withContext

interface ConfigurationRepository
{

    /**
     * Load the next N [Configurations]s that are before the given sequence number.
     * The [com.github.ericytsang.service.sqlite.Configuration]s are ordered by sequence number in descending order.
     */
    suspend fun loadNextNConfigurationIdsBefore(n:Int,sequenceNumber:Long):List<Configuration>

    /**
     * Load the next N configuration IDs that are after the given sequence number.
     * The Configuration IDs are ordered by sequence number in ascending order.
     */
    suspend fun loadNextNConfigurationIdsAfter(n:Int,sequenceNumber:Long):List<Configuration>

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
    override suspend fun loadNextNConfigurationIdsBefore(
        n:Int,
        sequenceNumber:Long,
    ):List<Configuration> = withContext(dispatchers.io)
    {
        queries.selectNConfigurationsUpdatedBefore(
            update_sequence = sequenceNumber,
            value_ = n.toLong(),
        ).executeAsList()
    }

    override suspend fun loadNextNConfigurationIdsAfter(
        n:Int,
        sequenceNumber:Long,
    ):List<Configuration> = withContext(dispatchers.io)
    {
        queries.selectNConfigurationsUpdatedAfter(
            update_sequence = sequenceNumber,
            value_ = n.toLong(),
        ).executeAsList().asReversed()
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
}