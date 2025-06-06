package com.github.ericytsang.domain.repo.repo

import app.cash.sqldelight.coroutines.asFlow
import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.domain.objects.FilterInterpretationMode
import com.github.ericytsang.domain.objects.FilterModel
import com.github.ericytsang.domain.objects.FilterModelId
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
    suspend fun insertFilter(
        configurationId:ConfigurationId,
        filterString:String,
        isCaseSensitive:Boolean,
        filterInterpretationMode:FilterInterpretationMode,
        filterType:FilterType,
        isActive:Boolean,
        orderIndex:OrderIndex,
    )

    fun selectFiltersForConfig(
        configurationId:ConfigurationId,
        filterType:FilterType,
    ):Flow<List<FilterModel>>

    suspend fun updateOrderIndex(
        filterModelId:FilterModelId,
        newOrderIndex:OrderIndex,
    )

    suspend fun updateFilterString(
        filterModelId:FilterModelId,
        newFilterString:String,
    )

    suspend fun updateIsCaseSensitive(
        filterModelId:FilterModelId,
        isCaseSensitive:Boolean,
    )

    suspend fun updateFilterInterpretationMode(
        filterModelId:FilterModelId,
        newFilterInterpretationMode:FilterInterpretationMode,
    )

    suspend fun updateIsActive(
        filterModelId:FilterModelId,
        isActive:Boolean,
    )
}

internal class FilterRepositoryImpl(
    kotlinDependencyProvider:KotlinDependencyProvider,
    databaseService:DatabaseService,
):FilterRepository,
    KotlinDependencyProvider by kotlinDependencyProvider,
    DatabaseService by databaseService
{

    override suspend fun insertFilter(
        configurationId:ConfigurationId,
        filterString:String,
        isCaseSensitive:Boolean,
        filterInterpretationMode:FilterInterpretationMode,
        filterType:FilterType,
        isActive:Boolean,
        orderIndex:OrderIndex,
    ) = withContext<Unit>(dispatchers.io)
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
    }

    override fun selectFiltersForConfig(
        configurationId:ConfigurationId,
        filterType:FilterType,
    ):Flow<List<FilterModel>> = queries
        .selectFiltersForConfig(
            config_id = configurationId.id,
            is_exclude_filter = filterType.toSqLiteLong(),
        )
        .asFlow()
        .map { it.executeAsList() }
        .map { list -> list.map { it.toDomainModel() } }

    override suspend fun updateOrderIndex(
        filterModelId:FilterModelId,
        newOrderIndex:OrderIndex,
    ) = withContext<Unit>(dispatchers.io)
    {
        queries.updateOrderIndex(
            id = filterModelId.id,
            order_index = newOrderIndex.orderIndex,
        )
    }

    override suspend fun updateFilterString(
        filterModelId:FilterModelId,
        newFilterString:String,
    ) = withContext<Unit>(dispatchers.io)
    {
        queries.updateFilterString(
            id = filterModelId.id,
            filter_string = newFilterString,
        )
    }

    override suspend fun updateIsCaseSensitive(
        filterModelId:FilterModelId,
        isCaseSensitive:Boolean,
    ) = withContext<Unit>(dispatchers.io)
    {
        queries.updateIsCaseSensitive(
            id = filterModelId.id,
            is_case_sensitive = isCaseSensitive.toSqLiteLong(),
        )
    }

    override suspend fun updateFilterInterpretationMode(
        filterModelId:FilterModelId,
        newFilterInterpretationMode:FilterInterpretationMode,
    ) = withContext<Unit>(dispatchers.io)
    {
        queries.updateFilterInterpretationMode(
            id = filterModelId.id,
            filter_interpretation_mode = newFilterInterpretationMode.toSqLiteText(),
        )
    }

    override suspend fun updateIsActive(
        filterModelId:FilterModelId,
        isActive:Boolean,
    ) = withContext<Unit>(dispatchers.io)
    {
        queries.updateIsActive(
            id = filterModelId.id,
            is_active = isActive.toSqLiteLong(),
        )
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
            id = FilterModelId(id),
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
