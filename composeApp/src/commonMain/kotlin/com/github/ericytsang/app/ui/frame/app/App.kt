package com.github.ericytsang.app.ui.frame.app


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.application
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.asset.IconEditLogFiles
import com.github.ericytsang.app.ui.asset.IconSettings
import com.github.ericytsang.app.ui.frame.newprojectwizard.NewProjectWizardWindow
import com.github.ericytsang.app.ui.frame.projectbrowser.ProjectBrowser
import com.github.ericytsang.app.ui.frame.projectbrowser.ProjectBrowserViewModel
import com.github.ericytsang.app.ui.frame.workingfileseteditor.ChildWindowManager
import com.github.ericytsang.app.ui.frame.workingfileseteditor.WorkingFileSetEditorViewModel
import com.github.ericytsang.app.ui.frame.workingfileseteditor.childWindowManager
import com.github.ericytsang.app.ui.modal.openWorkingFileSetEditorInNewWindowBlocking
import com.github.ericytsang.app.usecase.ThemeUseCase
import com.github.ericytsang.app.util.EnsureSingletonProcessInstance
import com.github.ericytsang.app.util.fillMaxBackground
import com.github.ericytsang.kotlin.ImmutableCoroutineScope.Companion.asImmutableCoroutineScope
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel

class App(
    kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
):KotlinDependencyProvider by kotlinDependencyProvider
{
    private val appCommandChannel = Channel<AppCommand>(capacity = Channel.UNLIMITED)
    private val doneLoadingSignalChannel = Channel<Unit>(capacity = Channel.UNLIMITED)

    fun main()
    {
        // asynchronously initialize the application
        applicationScope.launch(dispatchers.io)
        {
            // make sure only one instance of the application is running
            if (!EnsureSingletonProcessInstance().tryLock()) return@launch

            // figure out what the first action of the application should be
            val logViewerAppInit = LogViewerAppInit.create()
            val firstAction = logViewerAppInit.decideFirstAppAction()

            // perform the resolved action
            val logViewerApp = LogViewerApp.create(appCommandChannel)
            logViewerAppInit.perform(logViewerApp,firstAction)

            // hide the loading dialog because other app UIs are opened and should be visible now
            doneLoadingSignalChannel.send(Unit)
        }

        runApplication()
    }

    private fun runApplication() = application {

        // create a view model for the main loading dialog
        val uiScope = rememberCoroutineScope().asImmutableCoroutineScope()
        val mainLoadingDialogViewModel = remember {
            AppLoadingDialogViewModel(
                uiScope = uiScope,
                loadingFinishedSignalChannel = doneLoadingSignalChannel,
            )
        }

        // show a loading dialog right away until loading is finished
        println("// show a loading dialog right away")
        val shouldShowLoadingDialog by mainLoadingDialogViewModel.shouldShowLoadingDialogFlow.collectAsState(true)
        if (shouldShowLoadingDialog)
        {
            DialogWindow(
                onCloseRequest = { exitApplication() },
                title = "Log Viewer",
                alwaysOnTop = true,
                resizable = false,
                content = { fillMaxBackground { themeColors -> LoadingText(themeColors.onBackground) } },
            )
        }

        // manage child windows
        println("// manage child windows")
        val childWindowManager = childWindowManager(
            uniqueKeyPrefix = "main",
            onFinalWindowClosed = { exitApplication() },
        )

        // open windows upon receiving app commands
        println("// open windows upon receiving app commands")
        remember {
            AppOpenWindowsViewModel(
                uiScope = uiScope,
                appCommandChannel = appCommandChannel,
                handleCommand = { appCommand -> handleAppCommand(appCommand,childWindowManager) },
            )
        }
    }

    private fun handleAppCommand(
        appCommand:AppCommand,
        childWindowManager:ChildWindowManager,
    )
    {
        when (appCommand)
        {
            AppCommand.OpenNewProjectWizard -> childWindowManager.addChildWindow { controller ->
                NewProjectWizardWindow(controller)
            }
            is AppCommand.OpenProject -> childWindowManager.addChildWindow { controller ->
                NewProjectWizardWindow(controller)
            }
            AppCommand.OpenProjectBrowser -> childWindowManager.addChildWindow { controller ->
                NewProjectWizardWindow(controller)
            }
        }
    }
}




@Composable
fun AppOldUnused(
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
                    onClick = { /*openSettingsInNewWindowBlocking(window)*/ },
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
