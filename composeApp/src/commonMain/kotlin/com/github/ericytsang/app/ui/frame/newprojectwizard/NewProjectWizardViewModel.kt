package com.github.ericytsang.app.ui.frame.newprojectwizard

import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

interface NewProjectWizardViewModel
{
    val selectedFiles:Flow<List<SelectedFile>>
    fun addFiles(newFiles:List<SelectedFile>)
    fun moveFilesToPosition(files:List<SelectedFile>,position:Int)
    fun removeFiles(files:List<SelectedFile>)

    companion object
    {
        fun createDefault(
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
        ):NewProjectWizardViewModel = NewProjectWizardViewModelImpl(
            kotlinDependencyProvider = kotlinDependencyProvider,
        )
    }
}

data class SelectedFile(
    val filePath: String,
)

@OptIn(ExperimentalCoroutinesApi::class)
private class NewProjectWizardViewModelImpl(
    private val kotlinDependencyProvider:KotlinDependencyProvider,
):NewProjectWizardViewModel,
    KotlinDependencyProvider by kotlinDependencyProvider
{
    private val _selectedFiles = MutableStateFlow(emptyList<SelectedFile>())

    override val selectedFiles:Flow<List<SelectedFile>> = _selectedFiles

    override fun addFiles(newFiles:List<SelectedFile>)
    {
        applicationScope.launch(dispatchers.io)
        {
            _selectedFiles.update { selectedFiles -> selectedFiles + newFiles }
        }
    }

    override fun moveFilesToPosition(files:List<SelectedFile>,position:Int)
    {
        applicationScope.launch(dispatchers.io)
        {
            _selectedFiles.update()
            { selectedFiles ->

                // note the indices so that later, we can find the position of where the files should be moved to
                val indexedFiles = selectedFiles.withIndex()

                // remove files that are being moved
                val filesRemoved = indexedFiles.filter { it.value !in files }

                // separate the files that are before and after the position
                val (before, after) = filesRemoved.partition { it.index < position }

                // create a new list with the files moved to the specified position
                before.map { it.value } + files + after.map { it.value }
            }
        }
    }

    override fun removeFiles(files:List<SelectedFile>)
    {
        applicationScope.launch(dispatchers.io)
        {
            _selectedFiles.update { selectedFiles -> selectedFiles.filter { it !in files } }
        }
    }
}
