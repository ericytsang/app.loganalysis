package com.github.ericytsang.domain.repo.repo

import app.cash.sqldelight.coroutines.asFlow
import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.domain.objects.FilterInterpretationMode
import com.github.ericytsang.domain.objects.FilterModel
import com.github.ericytsang.domain.objects.FilterId
import com.github.ericytsang.domain.objects.FilterType
import com.github.ericytsang.domain.objects.OrderIndex
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.service.sqlite.FilterEntity
import com.github.ericytsang.service.sqlite.dbfactory.DatabaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface FilterRepository
{
    suspend fun insertFilterAtTop(
        configurationId:ConfigurationId,
        filterString:String,
        isCaseSensitive:Boolean,
        filterInterpretationMode:FilterInterpretationMode,
        filterType:FilterType,
        isActive:Boolean,
    ):Long

    suspend fun insertFilter(
        configurationId:ConfigurationId,
        filterString:String,
        isCaseSensitive:Boolean,
        filterInterpretationMode:FilterInterpretationMode,
        filterType:FilterType,
        isActive:Boolean,
        orderIndex:OrderIndex,
    ):Long

    fun onFiltersForConfigChanged(
        configurationId:ConfigurationId,
        filterType:FilterType,
    ):Flow<Unit>

    fun selectActiveFiltersForConfig(
        configurationId:ConfigurationId,
    ):Flow<List<FilterModel>>

    fun selectFiltersForConfig(
        configurationId:ConfigurationId,
        filterType:FilterType,
    ):Flow<List<FilterModel>>

    suspend fun moveFilter(
        idOfFilterBeingMoved:FilterId,
        idOfFilterAtDestination:FilterId,
    )

    suspend fun updateOrderIndex(
        filterId:FilterId,
        newOrderIndex:OrderIndex,
    )

    suspend fun updateFilterString(
        filterId:FilterId,
        newFilterString:String,
    )

    suspend fun updateIsCaseSensitive(
        filterId:FilterId,
        isCaseSensitive:Boolean,
    )

    suspend fun updateFilterInterpretationMode(
        filterId:FilterId,
        newFilterInterpretationMode:FilterInterpretationMode,
    )

    suspend fun updateIsActive(
        filterId:FilterId,
        isActive:Boolean,
    )

    suspend fun delete(
        filterId:FilterId,
    )
}

internal class FilterRepositoryImpl(
    kotlinDependencyProvider:KotlinDependencyProvider,
    databaseService:DatabaseService,
):FilterRepository,
    KotlinDependencyProvider by kotlinDependencyProvider,
    DatabaseService by databaseService
{
    override suspend fun insertFilterAtTop(
        configurationId:ConfigurationId,
        filterString:String,
        isCaseSensitive:Boolean,
        filterInterpretationMode:FilterInterpretationMode,
        filterType:FilterType,
        isActive:Boolean,
    ) = withContext<Long>(dispatchers.io)
    {
        transaction()
        {
            val currentMaxOrderIndex = queries
                .selectFiltersForConfigDesc(
                    config_id = configurationId.id,
                    is_exclude_filter = filterType.toSqLiteLong(),
                    order_index = Long.MAX_VALUE,
                    value_ = 1L,
                )
                .executeAsOneOrNull()
                ?.order_index ?: 0L
            queries.insertFilter(
                config_id = configurationId.id,
                filter_string = filterString,
                is_case_sensitive = isCaseSensitive.toSqLiteLong(),
                filter_interpretation_mode = filterInterpretationMode.toSqLiteText(),
                is_exclude_filter = filterType.toSqLiteLong(),
                is_active = isActive.toSqLiteLong(),
                order_index = currentMaxOrderIndex+1,
            )
            queries.lastInsertId().executeAsOne()
        }
    }

    override suspend fun insertFilter(
        configurationId:ConfigurationId,
        filterString:String,
        isCaseSensitive:Boolean,
        filterInterpretationMode:FilterInterpretationMode,
        filterType:FilterType,
        isActive:Boolean,
        orderIndex:OrderIndex,
    ) = withContext<Long>(dispatchers.io)
    {
        transaction()
        {
            queries.insertFilter(
                config_id = configurationId.id,
                filter_string = filterString,
                is_case_sensitive = isCaseSensitive.toSqLiteLong(),
                filter_interpretation_mode = filterInterpretationMode.toSqLiteText(),
                is_exclude_filter = filterType.toSqLiteLong(),
                is_active = isActive.toSqLiteLong(),
                order_index = orderIndex.orderIndex,
            )
            queries.lastInsertId().executeAsOne()
        }
    }

    override fun onFiltersForConfigChanged(
        configurationId:ConfigurationId,
        filterType:FilterType,
    ):Flow<Unit> = queries
        .selectAllFiltersForConfig(
            config_id = configurationId.id,
            is_exclude_filter = filterType.toSqLiteLong(),
        )
        .asFlow()
        .map { }

    override fun selectActiveFiltersForConfig(
        configurationId:ConfigurationId,
    ):Flow<List<FilterModel>> = queries
        .selectActiveFiltersForConfig(configurationId.id)
        .asFlow()
        .map { it.executeAsList() }
        .map { list -> list.map { it.toDomainModel() } }

    override fun selectFiltersForConfig(
        configurationId:ConfigurationId,
        filterType:FilterType,
    ):Flow<List<FilterModel>> = queries
        .selectAllFiltersForConfig(
            config_id = configurationId.id,
            is_exclude_filter = filterType.toSqLiteLong(),
        )
        .asFlow()
        .map { it.executeAsList() }
        .map { list -> list.map { it.toDomainModel() } }

    override suspend fun moveFilter(
        idOfFilterBeingMoved:FilterId,
        idOfFilterAtDestination:FilterId,
    )
    {
        transaction()
        {
            // get the filters from the database
            val filterBeingMoved = queries.selectFilterById(idOfFilterBeingMoved.id).executeAsOne()
            val filterAtDestination = queries.selectFilterById(idOfFilterAtDestination.id).executeAsOne()

            // see if both filters are in the same configuration and same filter type
            if (filterBeingMoved.config_id == filterAtDestination.config_id &&
                filterBeingMoved.is_exclude_filter == filterAtDestination.is_exclude_filter)
            {
                // if they are, we can just swap their order indices
                queries.updateOrderIndex(
                    id = idOfFilterBeingMoved.id,
                    order_index = filterAtDestination.order_index,
                )
                queries.updateOrderIndex(
                    id = idOfFilterAtDestination.id,
                    order_index = filterBeingMoved.order_index,
                )
            }
            else
            {
                // otherwise, delete the filter from the database
                queries.updateOrderIndex(
                    id = idOfFilterBeingMoved.id,
                    order_index = filterAtDestination.order_index + 1,
                )

                // move all filters that are after the destination filter down by one


                // add a new filter with the same properties as the one being moved in the destination configuration
            }
        }
    }

    override suspend fun updateOrderIndex(
        filterId:FilterId,
        newOrderIndex:OrderIndex,
    ) = withContext<Unit>(dispatchers.io)
    {
        transaction()
        {
            queries.updateOrderIndex(
                id = filterId.id,
                order_index = newOrderIndex.orderIndex,
            )
        }
    }

    override suspend fun updateFilterString(
        filterId:FilterId,
        newFilterString:String,
    ) = withContext<Unit>(dispatchers.io)
    {
        transaction()
        {
            queries.updateFilterString(
                id = filterId.id,
                filter_string = newFilterString,
            )
        }
    }

    override suspend fun updateIsCaseSensitive(
        filterId:FilterId,
        isCaseSensitive:Boolean,
    ) = withContext<Unit>(dispatchers.io)
    {
        transaction()
        {
            queries.updateIsCaseSensitive(
                id = filterId.id,
                is_case_sensitive = isCaseSensitive.toSqLiteLong(),
            )
        }
    }

    override suspend fun updateFilterInterpretationMode(
        filterId:FilterId,
        newFilterInterpretationMode:FilterInterpretationMode,
    ) = withContext<Unit>(dispatchers.io)
    {
        transaction()
        {
            queries.updateFilterInterpretationMode(
                id = filterId.id,
                filter_interpretation_mode = newFilterInterpretationMode.toSqLiteText(),
            )
        }
    }

    override suspend fun updateIsActive(
        filterId:FilterId,
        isActive:Boolean,
    ) = withContext<Unit>(dispatchers.io)
    {
        transaction()
        {
            queries.updateIsActive(
                id = filterId.id,
                is_active = isActive.toSqLiteLong(),
            )
        }
    }

    override suspend fun delete(filterId:FilterId) = withContext<Unit>(dispatchers.io)
    {
        transaction()
        {
            queries.deleteFilter(filterId.id)
        }
    }

    companion object
    {
        private val booleanBySqLiteInt = mapOf(
            0L to false,
            1L to true,
        )

        private val filterInterpretationModeBySqLiteText =
            FilterInterpretationMode.entries.associateBy { it.toSqLiteText() }

        private val filterTypeBySqLiteLong =
            FilterType.entries.associateBy { it.toSqLiteLong() }

        private fun Long.toBoolean() = booleanBySqLiteInt[this] ?: error("unexpected value: $this")

        private fun String.toFilterInterpretationMode() =
            filterInterpretationModeBySqLiteText[this] ?: error("unexpected value: $this")

        private fun Long.toFilterType() =
            filterTypeBySqLiteLong[this] ?: error("unexpected value: $this")

        private fun Boolean.toSqLiteLong() = when (this)
        {
            true -> 1L
            false -> 0L
        }

        private fun FilterInterpretationMode.toSqLiteText() = when (this)
        {
            FilterInterpretationMode.STRING_LITERAL -> "string_literal"
            FilterInterpretationMode.REGULAR_EXPRESSION -> "regex"
            FilterInterpretationMode.LOGCAT_FILTER -> "logcat_filter"
        }

        private fun FilterType.toSqLiteLong() = when (this)
        {
            FilterType.INCLUDE -> 0L
            FilterType.EXCLUDE -> 1L
        }

        private fun FilterEntity.toDomainModel() = FilterModel(
            id = FilterId(id),
            configurationId = ConfigurationId(config_id),
            filterString = filter_string,
            isCaseSensitive = is_case_sensitive.toBoolean(),
            filterInterpretationMode = filter_interpretation_mode.toFilterInterpretationMode(),
            filterType = is_exclude_filter.toFilterType(),
            isActive = is_active.toBoolean(),
            orderIndex = OrderIndex(order_index),
        )
    }
}
