package com.github.ericytsang.app.ui.frame.app


import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.application
import com.github.ericytsang.app.ui.util.component.LoadingText
import com.github.ericytsang.app.ui.frame.newprojectwizard.NewProjectWizardWindow
import com.github.ericytsang.app.ui.frame.projectbrowser.ProjectBrowserWindow
import com.github.ericytsang.app.ui.util.ChildWindowManager
import com.github.ericytsang.app.ui.util.childWindowManager
import com.github.ericytsang.app.util.EnsureSingletonProcessInstance
import com.github.ericytsang.app.ui.util.component.fillMaxBackground
import com.github.ericytsang.kotlin.ImmutableCoroutineScope.Companion.asImmutableCoroutineScope
import com.github.ericytsang.kotlin.KotlinDependencyProvider
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
            val firstAppActionComputer = FirstAppActionComputer.create()
            val firstAction = firstAppActionComputer.decideFirstAppAction()

            // perform the resolved action
            val appCommandHandler = AppCommandHandler.create(appCommandChannel)
            firstAppActionComputer.perform(appCommandHandler,firstAction)

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
                NewProjectWizardWindow(childWindowManager,controller)
            }
            is AppCommand.OpenProject -> childWindowManager.addChildWindow { controller ->
                NewProjectWizardWindow(childWindowManager,controller)
            }
            AppCommand.OpenProjectBrowser -> childWindowManager.addChildWindow { controller ->
                ProjectBrowserWindow(childWindowManager,controller)
            }
        }
    }
}
