package com.github.ericytsang.app.app

import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.domain.objects.WorkingFileSet
import com.github.ericytsang.domain.objects.WorkingFileSetEmpty
import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.WorkingFileSetRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.io.File

interface WorkingFileSetEditorViewModel
{
    val configurationId:StateFlow<ConfigurationId?>
    fun setConfigurationId(configurationId:ConfigurationId?)

    val workingFileSet:Flow<WorkingFileSet>
    fun addFiles(newFiles:List<File>)
    fun moveFilesToPosition(files:List<File>,position:Int)
    fun removeFiles(files:List<File>)

    companion object
    {
        fun createDefault(
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
            workingFileSetRepository:WorkingFileSetRepository = RepositoryDependencyProvider.instance.workingFileSetRepository,
        ):WorkingFileSetEditorViewModel = WorkingFileSetEditorViewModelImpl(
            kotlinDependencyProvider = kotlinDependencyProvider,
            workingFileSetRepository = workingFileSetRepository,
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private class WorkingFileSetEditorViewModelImpl(
    initialConfigurationId:ConfigurationId? = null,
    private val kotlinDependencyProvider:KotlinDependencyProvider,
    private val workingFileSetRepository:WorkingFileSetRepository,
):WorkingFileSetEditorViewModel,
    KotlinDependencyProvider by kotlinDependencyProvider
{
    private val _configurationId = MutableStateFlow(initialConfigurationId)
    override val configurationId:StateFlow<ConfigurationId?> get() = _configurationId

    override fun setConfigurationId(configurationId:ConfigurationId?)
    {
        _configurationId.value = configurationId
    }

    override val workingFileSet:Flow<WorkingFileSet> = configurationId
        .flatMapLatest<_,WorkingFileSet> { configurationId ->
            when (configurationId)
            {
                null -> flowOf(WorkingFileSetEmpty)
                else -> workingFileSetRepository.getWorkingFileSetFlow(configurationId)
            }
        }

    override fun addFiles(newFiles:List<File>)
    {
        applicationScope.launch(dispatchers.io)
        {
            createNewWorkingFileSetElse(newFiles)
            { configurationId ->
                workingFileSetRepository.addFiles(configurationId, newFiles)
            }
        }
    }

    override fun moveFilesToPosition(files:List<File>,position:Int)
    {
        applicationScope.launch(dispatchers.io)
        {
            createNewWorkingFileSetElse(files)
            { configurationId ->
                workingFileSetRepository.moveFilesToPosition(configurationId,files,position)
            }
        }
    }

    override fun removeFiles(files:List<File>)
    {
        applicationScope.launch(dispatchers.io)
        {
            createNewWorkingFileSetElse(emptyList())
            { configurationId ->
                workingFileSetRepository.removeFiles(configurationId,files)
            }
        }
    }

    private suspend fun createNewWorkingFileSetElse(newFiles:List<File>,orElse:suspend (ConfigurationId)->Unit)
    {
        val configurationId = _configurationId.value
        when (configurationId)
        {
            null -> _configurationId.value = workingFileSetRepository.createNewWorkingFileSet(newFiles)
            else -> orElse(configurationId)
        }
    }
}