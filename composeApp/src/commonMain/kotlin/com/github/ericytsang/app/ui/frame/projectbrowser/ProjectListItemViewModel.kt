package com.github.ericytsang.app.ui.frame.projectbrowser

import androidx.compose.material.Colors
import androidx.compose.material.ContentAlpha
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.github.ericytsang.app.ui.frame.projectbrowser.ProjectListItemViewModelImpl.ProjectItemTexts
import com.github.ericytsang.app.ui.frame.workingfileseteditor.ChildWindowManager
import com.github.ericytsang.app.ui.frame.workingfileseteditor.openLogViewer
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
    val name:Flow<ProjectItemTexts>
}

class ProjectListItemViewModelImpl(
    uiScope: CoroutineScope,
    private val rootChildWindowManager:ChildWindowManager,
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
        rootChildWindowManager.openLogViewer()
    }

    override fun openWorkingFileSetEditor()
    {

    }

    override val name:Flow<ProjectItemTexts> = flow()
    {
        // show "Loading..." until the name is available
        emit(secondaryText("Loading..."))

        // if there is a user-defined name, then show it
        if (project.configuration.name.name.isNotBlank())
        {
            emit(secondaryText(project.configuration.name.name))
            awaitCancellation()
        }

        // otherwise, load the files that were concatenated for this project
        val workingFileSet = workingFileSetRepository.getWorkingFileSetFlow(project.configuration.id).first()
        val filePaths = workingFileSet.files.map { it.file.absolutePath }

        // if there are no files, then show "<empty project>"
        if (filePaths.isEmpty())
        {
            emit(secondaryText("<empty project>"))
            awaitCancellation()
        }

        // if there is only one file, then show the file path
        if (filePaths.size == 1)
        {
            emit(secondaryText(filePaths.first()))
            awaitCancellation()
        }

        // some files were found. to shorten the display, we will show the common prefix of all file paths
        val commonPrefixAmongFilePaths = filePaths.fold(filePaths.first()) { acc,next -> acc.commonPrefixWith(next) }

        // there is no common prefix, so we will just show the list of file paths as they are
        if (commonPrefixAmongFilePaths.isEmpty())
        {
            emit(secondaryText(filePaths.joinToString()))
            awaitCancellation()
        }

        // there is a common prefix, so we will shorten the file paths to show only the part after the common prefix
        val shortenedFilePaths = filePaths.map { it.removePrefix(commonPrefixAmongFilePaths) }
        emit(secondaryAndBoldText("$commonPrefixAmongFilePaths... ",shortenedFilePaths))
    }.flowOn(dispatchers.io)
        .shareIn(uiScope,SharingStarted.WhileSubscribed(5.seconds))

    private fun secondaryText(text:String) = ProjectItemTexts(text)

    private fun secondaryAndBoldText(secondaryText:String, boldText:List<String>) =
        ProjectItemTexts(secondaryText, boldText)

    data class ProjectItemTexts(
        val secondaryText:String,
        val boldTexts:List<String> = emptyList(),
    )
    {
        @Composable
        fun toAnnotatedString(themeColors:Colors):AnnotatedString
        {
            return buildAnnotatedString {
                secondaryText(themeColors, secondaryText)
                for (i in boldTexts.indices)
                {
                    if (i > 0)
                    {
                        secondaryText(themeColors, ", ")
                    }
                    boldText(boldTexts[i])
                }
            }
        }

        @Composable
        private fun AnnotatedString.Builder.secondaryText(themeColors:Colors,secondaryText:String)
        {
            val secondaryTextColor = themeColors.onBackground.copy(alpha = ContentAlpha.medium)
            withStyle(SpanStyle(color = secondaryTextColor)) { append(secondaryText) }
        }

        private fun AnnotatedString.Builder.boldText(boldText:String)
        {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(boldText) }
        }
    }
}