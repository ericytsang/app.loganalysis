package com.github.ericytsang.app.ui.frame.projectbrowser

import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.ConfigurationRepository
import com.github.ericytsang.domain.repo.repo.WorkingFileSetRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

interface ProjectListItemViewModel
{
    /**
     * Function to delete the project associated with this item.
     */
    fun deleteProject()

    /**
     * Function to open the project in a log viewer window.
     */
    fun openProjectInLogViewer()

    /**
     * Function to open the working file set editor for this project.
     */
    fun openWorkingFileSetEditor()

    /**
     * The name to display for this project.
     */
    val name:Flow<String>
}

class ProjectListItemViewModelImpl(
    uiScope: CoroutineScope,
    private val project:ProjectListItemModel.Project,
    kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.Companion.instance,
    private val configurationRepository:ConfigurationRepository = RepositoryDependencyProvider.Companion.instance.configurationRepository,
    private val workingFileSetRepository:WorkingFileSetRepository = RepositoryDependencyProvider.Companion.instance.workingFileSetRepository,
):ProjectListItemViewModel,
    KotlinDependencyProvider by kotlinDependencyProvider
{
    override fun deleteProject()
    {
        applicationScope.launch(dispatchers.io)
        {
            configurationRepository.deleteConfiguration(project.configuration.id)
        }
    }

    override fun openProjectInLogViewer()
    {

    }

    override fun openWorkingFileSetEditor()
    {

    }

    override val name:Flow<String> = flow()
    {
        // show "Loading..." until the name is available
        emit("Loading...")

        // if there is a user-defined name, then show it
        if (project.configuration.name.name.isNotBlank())
        {
            emit(project.configuration.name.name)
            awaitCancellation()
        }

        // otherwise, load the files that were concatenated for this project
        val workingFileSet = workingFileSetRepository.getWorkingFileSetFlow(project.configuration.id).first()
        val filePaths = workingFileSet.files.map { it.file.absolutePath }

        // if there are no files, then show "<empty project>"
        if (filePaths.isEmpty())
        {
            emit("<empty project>")
            awaitCancellation()
        }

        // some files were found. to shorten the display, we will show the common prefix of all file paths
        val commonPrefixAmongFilePaths = filePaths.fold(filePaths.first()) { acc,next -> acc.commonPrefixWith(next) }

        // there is no common prefix, so we will just show the list of file paths as they are
        if (commonPrefixAmongFilePaths.isEmpty())
        {
            emit(filePaths.joinToString())
            awaitCancellation()
        }

        // there is a common prefix, so we will shorten the file paths to show only the part after the common prefix
        val shortenedFilePaths = filePaths.joinToString { it.removePrefix(commonPrefixAmongFilePaths) }
        emit("$commonPrefixAmongFilePaths... [$shortenedFilePaths]")
    }.flowOn(dispatchers.io)
        .shareIn(uiScope,SharingStarted.WhileSubscribed(5.seconds))
}