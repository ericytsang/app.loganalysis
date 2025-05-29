package org.example.project

import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.ericytsang.app.ui.frame.app.App
import com.github.ericytsang.app.ui.frame.app.LoadingText
import com.github.ericytsang.app.ui.frame.app.LogViewerApp
import com.github.ericytsang.app.ui.frame.app.LogViewerAppInit
import com.github.ericytsang.app.ui.frame.workingfileseteditor.WorkingFileSetEditor
import com.github.ericytsang.app.util.EnsureSingletonProcessInstance
import com.github.ericytsang.app.util.fillMaxBackground
import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class Main(
    kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
):KotlinDependencyProvider by kotlinDependencyProvider
{

    private val openNewProjectWizardRequestChannel = Channel<Unit>(capacity = Channel.UNLIMITED)
    private val openNewProjectWizardRequestFlow get() = openNewProjectWizardRequestChannel.receiveAsFlow()

    private val openProjectRequestChannel = Channel<ConfigurationId>(capacity = Channel.UNLIMITED)
    private val openProjectRequestFlow get() = openProjectRequestChannel.receiveAsFlow()

    private val openProjectBrowserRequestChannel = Channel<Unit>(capacity = Channel.UNLIMITED)
    private val openProjectBrowserRequestFlow get() = openProjectBrowserRequestChannel.receiveAsFlow()

    fun main() = application {

        // show a loading dialog right away
        var showDialogState by mutableStateOf(false)
        DialogWindow(
            onCloseRequest = { exitApplication() },
            title = "Log Viewer",
            resizable = true,
            visible = showDialogState,
        )
        {
            fillMaxBackground()
            { themeColors ->
                LoadingText()
            }
        }

        // asynchronously initialize the application
        applicationScope.launch(dispatchers.io)
        {
            // make sure only one instance of the application is running
            EnsureSingletonProcessInstance().acquireLock()

            // figure out what the first action of the application should be
            val logViewerAppInit = LogViewerAppInit.create()
            val firstAction = logViewerAppInit.decideFirstAppAction()

            // perform the resolved action
            val logViewerApp = LogViewerApp.create(
                openNewProjectWizardChannel = openNewProjectWizardRequestChannel,
                openProjectChannel = openProjectRequestChannel,
                openProjectBrowserChannel = openProjectBrowserRequestChannel,
            )
            logViewerAppInit.perform(logViewerApp,firstAction)

            // hide the loading dialog because other app UIs are opened and should be visible now
            showDialogState = false
            logViewerApp
        }

        // consume and handle view model requests
        val openNewProjectWizardRequest by openNewProjectWizardRequestFlow.collectAsState(null)
        val openProjectRequest by openProjectRequestFlow.collectAsState(null)
        val openProjectBrowserRequest by openProjectBrowserRequestFlow.collectAsState(null)

        // handle the request to open the new project wizard
        if (openNewProjectWizardRequest != null)
        {
            Window(
                onCloseRequest = ::exitApplication,
                title = "New project",
            )
            {
                fillMaxBackground()
                { themeColors ->
                    WorkingFileSetEditor(owner = window)
                }
            }
        }

        // handle the request to open a specific project
        if (openProjectRequest != null)
        {
            Window(
                onCloseRequest = ::exitApplication,
                title = "Specific Project - $openProjectRequest",
            )
            {
                fillMaxBackground()
                { themeColors ->
                    App(window = window)
                }
            }
        }

        // handle the request to open the project browser
        if (openProjectBrowserRequest != null)
        {
            Window(
                onCloseRequest = ::exitApplication,
                title = "Project Browser",
            )
            {
                fillMaxBackground()
                { themeColors ->
                    App(window = window)
                }
            }
        }
    }
}

fun main()
{
    Main().main()
}

private fun exampleMain() = application()
{
    Window(
        onCloseRequest = ::exitApplication,
        title = "Log Viewer",

        )
    {
        MenuBar()
        {
            Menu(
                text = "File",
                mnemonic = 'F',
            ) {
                Item(
                    text = "Open...",
                    mnemonic = 'O',
                    onClick = { /* TODO: Open log file */ }
                )
                Item(
                    text = "Recently opened...",
                    mnemonic = 'R',
                    onClick = { /* TODO: Open log file */ }
                )
                Item(
                    text = "Exit",
                    mnemonic = 'X',
                    onClick = { exitApplication() }
                )
            }
        }
        Text("test")
    }

    DialogWindow(
        onCloseRequest = { exitApplication() },
        title = "Log Viewer",
        resizable = true,
    )
    {
        Text("test2")
    }
}
