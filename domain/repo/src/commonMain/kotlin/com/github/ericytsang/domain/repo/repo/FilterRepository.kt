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
import kotlinx.coroutines.flow.conflate
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
    ):Flow<Unit>

    fun selectFilterById(
        filterId:FilterId,
    ):Flow<FilterModel>

    fun selectActiveFiltersForConfig(
        configurationId:ConfigurationId,
    ):Flow<List<FilterModel>>

    fun selectFiltersForConfig(
        configurationId:ConfigurationId,
    ):Flow<List<FilterModel>>

    suspend fun moveFilter(
        idOfFilterBeingMoved:FilterId,
        idOfFilterAtDestination:FilterId,
    )

    suspend fun moveFilterToTopOfSection(
        idOfFilterBeingMoved:FilterId,
        destinationSectionFilterType:FilterType
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
    ):Flow<Unit> = queries
        .selectAllFiltersForConfig(config_id = configurationId.id)
        .asFlow()
        .map { }

    override fun selectFilterById(
        filterId:FilterId,
    ):Flow<FilterModel> = queries
        .selectFilterById(filterId.id)
        .asFlow()
        .conflate()
        .map { it.executeAsOne() }
        .map { it.toDomainModel() }

    override fun selectActiveFiltersForConfig(
        configurationId:ConfigurationId,
    ):Flow<List<FilterModel>> = queries
        .selectActiveFiltersForConfig(configurationId.id)
        .asFlow()
        .map { it.executeAsList() }
        .map { list -> list.map { it.toDomainModel() } }

    override fun selectFiltersForConfig(
        configurationId:ConfigurationId,
    ):Flow<List<FilterModel>> = queries
        .selectAllFiltersForConfig(
            config_id = configurationId.id,
        )
        .asFlow()
        .conflate()
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

            // move the filter being moved to a temporary order index
            queries.updateOrderIndex(
                id = filterBeingMoved.id,
                order_index = Long.MIN_VALUE,
            )

            // algorithm when filters are in the same configuration and filter type:
            if (filterBeingMoved.config_id == filterAtDestination.config_id &&
                filterBeingMoved.is_exclude_filter == filterAtDestination.is_exclude_filter)
            {

                // decide if we need to increment or decrement the order index of other filters,
                // which depends on whether the filter being moved to a higher or lower order index
                val shouldIncrementOrderIndexOfOthers = filterBeingMoved.order_index > filterAtDestination.order_index

                // increment or decrement the order index of other filters
                if (shouldIncrementOrderIndexOfOthers)
                {
                    updateOrderIndexBulkIncrement(
                        orderIndexRange = OrderIndex(filterAtDestination.order_index)..OrderIndex(filterBeingMoved.order_index),
                        configurationId = ConfigurationId(filterAtDestination.config_id),
                        filterType = filterAtDestination.is_exclude_filter.toFilterType(),
                    )
                }
                else
                {
                    updateOrderIndexBulkDecrement(
                        orderIndexRange = OrderIndex(filterBeingMoved.order_index)..OrderIndex(filterAtDestination.order_index),
                        configurationId = ConfigurationId(filterAtDestination.config_id),
                        filterType = filterAtDestination.is_exclude_filter.toFilterType(),
                    )
                }
            }

            // algorithm when filters are in different configurations or filter types:
            else
            {
                // increment all the filters that are at or higher than the destination order index
                updateOrderIndexBulkIncrement(
                    orderIndexRange = OrderIndex(filterAtDestination.order_index)..OrderIndex(Long.MAX_VALUE),
                    configurationId = ConfigurationId(filterAtDestination.config_id),
                    filterType = filterAtDestination.is_exclude_filter.toFilterType(),
                )
            }

            // update the filter being moved to move it to the destination
            queries.updateFilterForMove(
                id = filterBeingMoved.id,
                order_index = filterAtDestination.order_index,
                is_exclude_filter = filterAtDestination.is_exclude_filter,
                config_id = filterAtDestination.config_id,
            )
        }
    }

    private fun updateOrderIndexBulkIncrement(
        orderIndexRange:ClosedRange<OrderIndex>,
        configurationId:ConfigurationId,
        filterType:FilterType,
        pageSize:Int = 10,
    )
    {
        // select the next pageSize filters that are in the range
        val filters = queries.selectNFiltersAtOrLtOrderIndex(
            config_id = configurationId.id,
            is_exclude_filter = filterType.toSqLiteLong(),
            order_index = orderIndexRange.endInclusive.orderIndex,
            value_ = pageSize.toLong(),
        ).executeAsList()

        // for the filters in the range, increment their order index by 1
        val filtersInRange = filters.filter { OrderIndex(it.order_index) in orderIndexRange }
        filtersInRange.forEach { filter ->
            queries.updateOrderIndex(
                id = filter.id,
                order_index = filter.order_index + 1,
            )
        }

        // call recursively, but with a smaller range, to continue processing until no more filters in the range
        val newEndIndex = filtersInRange.minOfOrNull { it.order_index }?.minus(1)?.let { OrderIndex(it) } ?: return
        updateOrderIndexBulkIncrement(
            orderIndexRange = orderIndexRange.start..newEndIndex,
            configurationId = configurationId,
            filterType = filterType,
            pageSize = pageSize,
        )
    }

    private fun updateOrderIndexBulkDecrement(
        orderIndexRange:ClosedRange<OrderIndex>,
        configurationId:ConfigurationId,
        filterType:FilterType,
        pageSize:Int = 10,
    )
    {
        // select the next pageSize filters that are in the range
        val filters = queries.selectNFiltersAtOrGtOrderIndex(
            config_id = configurationId.id,
            is_exclude_filter = filterType.toSqLiteLong(),
            order_index = orderIndexRange.start.orderIndex,
            value_ = pageSize.toLong(),
        ).executeAsList()

        // for the filters in the range, decrement their order index by 1
        val filtersInRange = filters.filter { OrderIndex(it.order_index) in orderIndexRange }
        filtersInRange.forEach { filter ->
            queries.updateOrderIndex(
                id = filter.id,
                order_index = filter.order_index - 1,
            )
        }

        // call recursively, but with a smaller range, to continue processing until no more filters in the range
        val newStartIndex = filtersInRange.maxOfOrNull { it.order_index }?.plus(1)?.let { OrderIndex(it) } ?: return
        updateOrderIndexBulkDecrement(
            orderIndexRange = newStartIndex..orderIndexRange.endInclusive,
            configurationId = configurationId,
            filterType = filterType,
            pageSize = pageSize,
        )
    }

    override suspend fun moveFilterToTopOfSection(
        idOfFilterBeingMoved:FilterId,
        destinationSectionFilterType:FilterType,
    ) = withContext<Unit>(dispatchers.io)
    {
        transaction()
        {
            // get the filter being moved
            val filterBeingMoved = queries.selectFilterById(idOfFilterBeingMoved.id).executeAsOne()

            // get the max order index of the filters in the destination section
            val currentMaxOrderRow = queries
                .selectFiltersForConfigDesc(
                    config_id = filterBeingMoved.config_id,
                    is_exclude_filter = destinationSectionFilterType.toSqLiteLong(),
                    order_index = Long.MAX_VALUE,
                    value_ = 1L,
                )
                .executeAsOneOrNull()

            // if the filter being moved is already at the top of the section, do nothing
            if (currentMaxOrderRow?.id == filterBeingMoved.id)
            {
                return@transaction
            }

            // move the filter to the top of the section
            queries.updateFilterForMove(
                id = filterBeingMoved.id,
                order_index = (currentMaxOrderRow?.order_index ?: 0L) + 1L,
                is_exclude_filter = destinationSectionFilterType.toSqLiteLong(),
                config_id = filterBeingMoved.config_id,
            )
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
            FilterType.BOOKMARK -> 2L
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
