package com.github.ericytsang.app.ui.frame.logviewer

import com.github.ericytsang.domain.objects.ConfigurationId
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
    suspend fun reorderFilters(fromIndex:Int,toIndex:Int)
}

@OptIn(ExperimentalCoroutinesApi::class)
class FilterTypeFilterSetViewModel(
    private val filterType:FilterType,
    private val configurationId:ConfigurationId,
    private val filterRepo:FilterRepository = RepositoryDependencyProvider.Companion.instance.filterRepository,
    kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.Companion.instance,
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

    override suspend fun reorderFilters(fromIndex:Int,toIndex:Int) = withContext(dispatchers.io)
    {
    }
}
