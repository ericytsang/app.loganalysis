package com.github.ericytsang.app.ui.frame.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.ApplicationScope
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.asset.IconEditLogFiles
import com.github.ericytsang.app.ui.asset.IconSettings
import com.github.ericytsang.app.ui.frame.app.LogViewerAppInitImpl
import com.github.ericytsang.app.ui.frame.projectbrowser.ProjectBrowser
import com.github.ericytsang.app.ui.frame.projectbrowser.ProjectBrowserViewModel
import com.github.ericytsang.app.ui.frame.workingfileseteditor.WorkingFileSetEditorViewModel
import com.github.ericytsang.app.ui.modal.openSettingsInNewWindowBlocking
import com.github.ericytsang.app.ui.modal.openWorkingFileSetEditorInNewWindowBlocking
import com.github.ericytsang.app.usecase.ThemeUseCase
import com.github.ericytsang.app.util.fillMaxBackground
import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.domain.objects.ConfigurationUpdateSequence
import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.ConfigurationRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Window

class LogViewerAppInitImpl(
    kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
    private val configurationRepository:ConfigurationRepository = RepositoryDependencyProvider.instance.configurationRepository,
):LogViewerAppInit,
    KotlinDependencyProvider by kotlinDependencyProvider
{
    override suspend fun decideFirstAppAction():FirstAppAction = withContext(dispatchers.io)
    {
        // if there are no projects at all, then open the "New Project" wizard
        val oldProjects = configurationRepository.loadNextNConfigurationIdsBefore(1,ConfigurationUpdateSequence.max)
        if (oldProjects.isEmpty())
        {
            return@withContext FirstAppAction.OpenNewProjectWizard
        }

        // if there are projects that are active, then open them up
        val activeProjects = configurationRepository.getActiveProjects()
        if (activeProjects.hasNext())
        {
            return@withContext FirstAppAction.OpenPreviouslyOpenedProjects
        }

        // if there are projects, but none of them were active, then open the project browser
        return@withContext FirstAppAction.OpenProjectBrowser
    }

    override suspend fun perform(logViewerApp:LogViewerApp, firstAppAction:FirstAppAction)
    {
        when (firstAppAction)
        {
            is FirstAppAction.OpenNewProjectWizard -> logViewerApp.openNewProjectWizard()
            is FirstAppAction.OpenPreviouslyOpenedProjects ->
            {
                val activeProjects = configurationRepository.getActiveProjects()
                while (activeProjects.hasNext())
                {
                    logViewerApp.openProject(activeProjects.next().id)
                }
            }
            is FirstAppAction.OpenProjectBrowser -> logViewerApp.openProjectBrowser()
        }
    }
}

interface LogViewerAppInit
{
    suspend fun decideFirstAppAction():FirstAppAction
    suspend fun perform(logViewerApp:LogViewerApp,firstAppAction:FirstAppAction)

    companion object
    {
        fun create():LogViewerAppInit = LogViewerAppInitImpl()
    }
}

interface LogViewerApp
{
    fun openNewProjectWizard()
    fun openProject(configurationId:ConfigurationId)
    fun openProjectBrowser()

    companion object
    {
        fun create(
            openNewProjectWizardChannel:SendChannel<Unit>,
            openProjectChannel:SendChannel<ConfigurationId>,
            openProjectBrowserChannel:SendChannel<Unit>,
        ):LogViewerApp = LogViewerAppImpl(
            openNewProjectWizardChannel = openNewProjectWizardChannel,
            openProjectChannel = openProjectChannel,
            openProjectBrowserChannel = openProjectBrowserChannel,
        )
    }
}

class LogViewerAppImpl(
    private val openNewProjectWizardChannel:SendChannel<Unit>,
    private val openProjectChannel:SendChannel<ConfigurationId>,
    private val openProjectBrowserChannel:SendChannel<Unit>,
):LogViewerApp
{
    override fun openNewProjectWizard()
    {
        openNewProjectWizardChannel.trySendBlocking(Unit)
    }

    override fun openProject(configurationId:ConfigurationId)
    {
        openProjectChannel.trySendBlocking(configurationId)
    }

    override fun openProjectBrowser()
    {
        openProjectBrowserChannel.trySendBlocking(Unit)
    }
}

sealed class FirstAppAction
{
    /**
     * Opens the "New Project" wizard.
     * This is the default action when the app is launched, and there are no projects at all.
     * It will show a UI allowing the user to choose what log files to include in the project,
     * and letting them re-order the log files, too.
     */
    data object OpenNewProjectWizard:FirstAppAction()

    /**
     * Opens all previously opened projects.
     * If upon the last app close, there were still projects that were opened, then this action will be taken
     * to restore the state of the app to what it was before the last close.
     */
    data object OpenPreviouslyOpenedProjects:FirstAppAction()

    /**
     * If there were some projects, but none of them were opened upon the last app close,
     * then this action will be taken to open the project browser, allowing the user to
     * pick a previous project to load, or to open the "New Project" wizard,
     */
    data object OpenProjectBrowser:FirstAppAction()
}

@Composable
fun LoadingText(kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance)
{
    var animationFrame by mutableStateOf(0)
    kotlinDependencyProvider.applicationScope.launch {
        delay(500)
        animationFrame++
    }
    val dots = (0 until (animationFrame % 4)).joinToString("") { "." }
    Text(
        text = "Loading${dots}",
        modifier = Modifier.padding(Dimens.mttPadding),
    )
}

@Composable
fun App(
    window:ComposeWindow,
    themeUseCaseFactory:()->ThemeUseCase = { ThemeUseCase.instance },
    projectBrowserViewModelFactory:(CoroutineScope)->ProjectBrowserViewModel = { ProjectBrowserViewModel.create() },
    workingFileSetEditorViewModelFactory:()->WorkingFileSetEditorViewModel = { WorkingFileSetEditorViewModel.createDefault() },
)
{
    val workingFileSetEditorViewModel = remember { workingFileSetEditorViewModelFactory() }

    val themeUseCase = remember { themeUseCaseFactory() }
    fillMaxBackground()
    { themeColors ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        )
        {
            Row(
                modifier = Modifier.fillMaxWidth().padding(Dimens.mttPadding),
            ) {
                Button(
                    modifier = Modifier.padding(end = Dimens.mttPadding),
                    onClick =
                    {
                        openWorkingFileSetEditorInNewWindowBlocking(
                            owner = window,
                            viewModel = workingFileSetEditorViewModel,
                            themeUseCase = themeUseCase,
                        )
                    },
                    content = { IconEditLogFiles(themeColors.onPrimary) },
                )
                Button(
                    onClick = { openSettingsInNewWindowBlocking(window) },
                    content = { IconSettings(themeColors.onPrimary) },
                )
            }
            ProjectBrowser(
                viewModelFactory = projectBrowserViewModelFactory,
                paddingValues = PaddingValues(
                    start = Dimens.mttPadding,
                    end = Dimens.mttPadding,
                    bottom = Dimens.mttPadding,
                )
            )
        }
    }
}

