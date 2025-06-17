package com.github.ericytsang.app.ui.frame.logviewer

import com.github.ericytsang.domain.objects.FilterId
import com.github.ericytsang.domain.objects.FilterInterpretationMode
import com.github.ericytsang.domain.objects.FilterType
import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.FilterRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface FilterViewModel
{
    val filterId:FilterId

    val filterStringFlow:Flow<String>
    val isCaseSensitiveFlow:Flow<Boolean>
    val isEnabledFlow:Flow<Boolean>
    val filterInterpretationModeFlow:Flow<FilterInterpretationMode>

    fun setFilterString(newValue:String)
    fun setCaseSensitive(newValue:Boolean)
    fun setEnabled(newValue:Boolean)
    fun setFilterType(newValue:FilterInterpretationMode)
    fun requestDelete()
}

class FilterViewModelImpl(
    override val filterId:FilterId,
    initialFilterString:String,
    initialIsCaseSensitive:Boolean,
    initialIsEnabled:Boolean,
    initialFilterInterpretationMode:FilterInterpretationMode,
    private val onRequestDelete:(FilterId)->Unit,
    filterRepository:FilterRepository = RepositoryDependencyProvider.Companion.instance.filterRepository,
    kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.Companion.instance,
):FilterViewModel,
    KotlinDependencyProvider by kotlinDependencyProvider
{

    private val _filterStringFlow = PersistedValue(
        initialValue = initialFilterString,
        updatePersistedValue = { newValue -> filterRepository.updateFilterString(filterId, newValue) },
    )

    override val filterStringFlow:Flow<String> get() = _filterStringFlow.valueFlow

    override fun setFilterString(newValue:String)
    {
        _filterStringFlow.value = newValue
    }

    private val _isCaseSensitiveFlow = PersistedValue(
        initialValue = initialIsCaseSensitive,
        updatePersistedValue = { newValue -> filterRepository.updateIsCaseSensitive(filterId, newValue) },
    )

    override val isCaseSensitiveFlow:Flow<Boolean> get() = _isCaseSensitiveFlow.valueFlow

    override fun setCaseSensitive(newValue:Boolean)
    {
        _isCaseSensitiveFlow.value = newValue
    }

    private val _isEnabledFlow = PersistedValue(
        initialValue = initialIsEnabled,
        updatePersistedValue = { newValue -> filterRepository.updateIsActive(filterId, newValue) },
    )

    override val isEnabledFlow:Flow<Boolean> get() = _isEnabledFlow.valueFlow

    override fun setEnabled(newValue:Boolean)
    {
        _isEnabledFlow.value = newValue
    }

    private val _filterInterpretationModeFlow = PersistedValue(
        initialValue = initialFilterInterpretationMode,
        updatePersistedValue = { newValue -> filterRepository.updateFilterInterpretationMode(filterId, newValue) },
    )

    override val filterInterpretationModeFlow:Flow<FilterInterpretationMode> get() = _filterInterpretationModeFlow.valueFlow

    override fun setFilterType(newValue:FilterInterpretationMode)
    {
        _filterInterpretationModeFlow.value = newValue
    }

    override fun requestDelete()
    {
        onRequestDelete(filterId)
    }
}