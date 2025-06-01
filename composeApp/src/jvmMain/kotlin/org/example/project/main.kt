package org.example.project

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.application
import com.github.ericytsang.app.ui.frame.app.AppCommand
import com.github.ericytsang.app.ui.frame.app.LoadingText
import com.github.ericytsang.app.ui.frame.app.LogViewerApp
import com.github.ericytsang.app.ui.frame.app.LogViewerAppInit
import com.github.ericytsang.app.ui.frame.app.NewProjectWizardWindow
import com.github.ericytsang.app.ui.frame.workingfileseteditor.ChildWindowManager
import com.github.ericytsang.app.ui.frame.workingfileseteditor.childWindowManager
import com.github.ericytsang.app.util.EnsureSingletonProcessInstance
import com.github.ericytsang.app.util.fillMaxBackground
import com.github.ericytsang.kotlin.ImmutableCoroutineScope.Companion.asImmutableCoroutineScope
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.channels.Channel

class Main(
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
            MainLoadingDialogViewModel(
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
                content = { fillMaxBackground { themeColors -> LoadingText() } },
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
            MainOpenWindowsViewModel(
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

fun main()
{
    Main().main()
}
