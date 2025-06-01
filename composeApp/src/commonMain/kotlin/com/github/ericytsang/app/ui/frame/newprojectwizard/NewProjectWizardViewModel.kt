package com.github.ericytsang.app.ui.frame.newprojectwizard

import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.WorkingFileSetRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

interface NewProjectWizardViewModel
{
    val selectedFiles:Flow<List<SelectedFile>>
    fun addFiles(newFiles:List<SelectedFile>)
    fun moveFilesToPosition(files:List<SelectedFile>,position:Int)
    fun removeFiles(files:List<SelectedFile>)

    /**
     * this is a request from the UI to the view model to create a new configuration
     * with the currently selected files.
     * the view model will then start creating the configuration in the background,
     * and then notify the UI when the configuration is created via [launchLogViewerRequest].
     */
    fun onDoneButtonClicked()

    /**
     * once the "Done" button is clicked, the view model will
     * start creating the configuration in the background.
     * during this time, the UI should...
     * - todo: show a loading indicator,
     * - and disable any UI components for modifying the configuration.
     */
    val isCreatingConfiguration:Flow<Boolean>

    /**
     * this is a request from the view model to the UI to launch the log
     * viewer for the newly created configuration.
     */
    val launchLogViewerRequest:ReceiveChannel<ConfigurationId>

    companion object
    {
        fun createDefault():NewProjectWizardViewModel = NewProjectWizardViewModelImpl(
            kotlinDependencyProvider = KotlinDependencyProvider.instance,
            workingFileSetRepository = RepositoryDependencyProvider.instance.workingFileSetRepository,
        )
    }
}

data class SelectedFile(
    val filePath: String,
)

@OptIn(ExperimentalCoroutinesApi::class)
private class NewProjectWizardViewModelImpl(
    private val kotlinDependencyProvider:KotlinDependencyProvider,
    private val workingFileSetRepository:WorkingFileSetRepository,
):NewProjectWizardViewModel,
    KotlinDependencyProvider by kotlinDependencyProvider
{

    private val _isCreatingConfiguration = MutableStateFlow(false)
    override val isCreatingConfiguration:Flow<Boolean> get() = _isCreatingConfiguration

    private val _launchLogViewerRequest = Channel<ConfigurationId>()
    override val launchLogViewerRequest:ReceiveChannel<ConfigurationId> get() = _launchLogViewerRequest

    private val _selectedFiles = MutableStateFlow(emptyList<SelectedFile>())
    override val selectedFiles:Flow<List<SelectedFile>> = _selectedFiles

    override fun addFiles(newFiles:List<SelectedFile>)
    {
        applicationScope.launch(dispatchers.io)
        {
            _selectedFiles.update { selectedFiles -> selectedFiles + newFiles.filter { it !in selectedFiles } }
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

    override fun onDoneButtonClicked()
    {
        synchronized(_isCreatingConfiguration)
        {
            if (_isCreatingConfiguration.value)
                return // already creating a configuration, ignore this request

            _isCreatingConfiguration.value = true

            applicationScope.launch(dispatchers.io)
            {
                try
                {
                    // create the configuration with the selected files
                    val newFiles = _selectedFiles.value.map { File(it.filePath) }
                    val newConfigurationId = workingFileSetRepository.createNewWorkingFileSet(newFiles)

                    // notify the UI to launch the log viewer for the newly created configuration
                    _launchLogViewerRequest.send(newConfigurationId)
                }
                finally
                {
                    // done creating configuration
                    _isCreatingConfiguration.value = false
                }
            }
        }
    }
}
