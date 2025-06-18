package com.github.ericytsang.app.ui.frame.workingfileseteditor

import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.domain.objects.FilePath
import com.github.ericytsang.domain.objects.FilePath.Companion.toFilePath
import com.github.ericytsang.domain.objects.WorkingFileSet
import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.WorkingFileSetRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import java.io.File

interface WorkingFileSetEditorViewModel
{
    val configurationId:ConfigurationId
    val workingFileSet:Flow<WorkingFileSet>
    fun addFiles(newFiles:List<File>)
    fun moveFilesToPosition(files:List<File>,position:Int)
    fun removeFile(file:File)

    companion object
    {
        fun create(
            configurationId:ConfigurationId,
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
            workingFileSetRepository:WorkingFileSetRepository = RepositoryDependencyProvider.instance.workingFileSetRepository,
        ):WorkingFileSetEditorViewModel = WorkingFileSetEditorViewModelImpl(
            configurationId = configurationId,
            kotlinDependencyProvider = kotlinDependencyProvider,
            workingFileSetRepository = workingFileSetRepository,
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private class WorkingFileSetEditorViewModelImpl(
    override val configurationId:ConfigurationId,
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

    override fun moveFilesToPosition(files:List<File>,position:Int)
    {
        applicationScope.launch(dispatchers.io)
        {
            workingFileSetRepository.moveFilesToPosition(configurationId,files,position)
        }
    }

    override fun removeFile(file:File)
    {
        applicationScope.launch(dispatchers.io)
        {
            workingFileSetRepository.removeFile(configurationId,file.toFilePath())
        }
    }
}



