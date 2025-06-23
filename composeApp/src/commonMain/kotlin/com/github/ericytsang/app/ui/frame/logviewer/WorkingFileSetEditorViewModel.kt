package com.github.ericytsang.app.ui.frame.logviewer

import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.domain.objects.WorkingFileSet
import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.WorkingFileSetRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import java.io.File

interface WorkingFileSetEditorViewModel
{
    val workingFileSet:Flow<WorkingFileSet>
    fun addFiles(newFiles:List<File>)

    companion object
    {
        fun create(
            configurationId:ConfigurationId,
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.Companion.instance,
            workingFileSetRepository:WorkingFileSetRepository = RepositoryDependencyProvider.Companion.instance.workingFileSetRepository,
        ):WorkingFileSetEditorViewModel = WorkingFileSetEditorViewModelImpl(
            configurationId = configurationId,
            kotlinDependencyProvider = kotlinDependencyProvider,
            workingFileSetRepository = workingFileSetRepository,
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private class WorkingFileSetEditorViewModelImpl(
    private val configurationId:ConfigurationId,
    private val kotlinDependencyProvider:KotlinDependencyProvider,
    private val workingFileSetRepository:WorkingFileSetRepository,
):WorkingFileSetEditorViewModel,
    KotlinDependencyProvider by kotlinDependencyProvider
{
    override val workingFileSet:Flow<WorkingFileSet> = workingFileSetRepository.getWorkingFileSetFlow(configurationId)

    override fun addFiles(newFiles:List<File>)
    {
        applicationScope.launch(dispatchers.io)
        {
            workingFileSetRepository.addFiles(configurationId,newFiles)
        }
    }
}
