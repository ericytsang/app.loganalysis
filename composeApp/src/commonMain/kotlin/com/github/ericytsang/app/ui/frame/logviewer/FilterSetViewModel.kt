package com.github.ericytsang.app.ui.frame.logviewer

import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.domain.objects.FilterId
import com.github.ericytsang.domain.objects.FilterInterpretationMode
import com.github.ericytsang.domain.objects.FilterType
import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.FilterRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext

interface FilterSetViewModel
{
    val filters:Flow<List<FilterViewModel>>
    fun addFilter()
}

@OptIn(ExperimentalCoroutinesApi::class)
class FilterTypeFilterSetViewModel(
    private val filterType:FilterType,
    private val configurationId:ConfigurationId,
    private val filterRepo:FilterRepository = RepositoryDependencyProvider.instance.filterRepository,
    kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
):FilterSetViewModel,
    KotlinDependencyProvider by kotlinDependencyProvider
{
    override val filters:Flow<List<FilterViewModel>> = filterRepo.selectFiltersForConfig(
        configurationId = configurationId,
        filterType = filterType,
    ).mapLatest { rows ->
        rows.map { row ->
            FilterViewModelImpl(
                filterId = row.id,
                initialFilterString = row.filterString,
                initialIsCaseSensitive = row.isCaseSensitive,
                initialIsEnabled = row.isActive,
                initialFilterInterpretationMode = row.filterInterpretationMode,
                onRequestDelete = { filterId -> applicationScope.launch { filterRepo.delete(row.id) } },
            )
        }
    }

    override fun addFilter()
    {
        applicationScope.launch(dispatchers.io)
        {
            filterRepo.insertFilterAtTop(
                configurationId = configurationId,
                filterString = "",
                isCaseSensitive = false,
                filterInterpretationMode = FilterInterpretationMode.STRING_LITERAL,
                filterType = filterType,
                isActive = true,
            )
        }
    }
}

interface ReorderSidebarItemViewModel
{
    suspend fun moveItem(from:ReorderableSidebarItemKey,to:ReorderableSidebarItemKey)

    companion object
    {
        fun create(
            configurationId:ConfigurationId,
        ):ReorderSidebarItemViewModel = ReorderSidebarItemViewModelImpl(
            configurationId = configurationId,
        )
    }
}

class ReorderSidebarItemViewModelImpl(
    private val configurationId:ConfigurationId,
    private val filterRepo:FilterRepository = RepositoryDependencyProvider.instance.filterRepository,
    private val kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
):ReorderSidebarItemViewModel,KotlinDependencyProvider by kotlinDependencyProvider
{
    override suspend fun moveItem(from:ReorderableSidebarItemKey,to:ReorderableSidebarItemKey) = withContext(dispatchers.io)
    {
        when (from)
        {
            // non reorderable item, do nothing
            is ReorderableSidebarItemKey.Other -> return@withContext

            // "new filter" button can only be used as a target, and cannot be moved
            is ReorderableSidebarItemKey.NewFilterButton -> error("cannot move 'new filter' button")

            is ReorderableSidebarItemKey.FilterItem -> when (to)
            {
                // non reorderable item, do nothing
                is ReorderableSidebarItemKey.Other -> return@withContext

                // move filter to the target position
                is ReorderableSidebarItemKey.FilterItem -> filterRepo.moveFilter(from.filterId,to.filterId)

                // move filter to the top of the section
                is ReorderableSidebarItemKey.NewFilterButton ->
                    filterRepo.moveFilterToTopOfSection(from.filterId, to.filterType)
            }
        }
    }
}

sealed interface ReorderableSidebarItemKey
{
    /** key for non reorderable item */
    data class Other(val key:String):ReorderableSidebarItemKey

    /**
     * key for the "new filter" button which will be treated as the user
     * trying to add the filter to the top of the section.
     */
    data class NewFilterButton(val filterType:FilterType):ReorderableSidebarItemKey

    /** key for reorderable filter item */
    data class FilterItem(val filterId:FilterId):ReorderableSidebarItemKey
}
