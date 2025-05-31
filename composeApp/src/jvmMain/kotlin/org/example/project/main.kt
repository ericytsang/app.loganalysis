package org.example.project

import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.ericytsang.app.ui.frame.app.AppCommand
import com.github.ericytsang.app.ui.frame.app.LoadingText
import com.github.ericytsang.app.ui.frame.app.LogViewerApp
import com.github.ericytsang.app.ui.frame.app.LogViewerAppInit
import com.github.ericytsang.app.ui.frame.app.WindowContentParams
import com.github.ericytsang.app.ui.frame.app.WindowInfo
import com.github.ericytsang.app.ui.frame.app.WindowInterface
import com.github.ericytsang.app.ui.frame.app.openNewProjectWizard
import com.github.ericytsang.app.ui.frame.app.openOpenProject
import com.github.ericytsang.app.ui.frame.app.openOpenProjectBrowser
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

        // keep track of all open windows
        println("// keep track of all open windows")
        var openWindows by remember { mutableStateOf<Map<Long,WindowInfo>>(emptyMap()) }
        remember {
            MainOpenWindowsViewModel(
                uiScope = uiScope,
                appCommandChannel = appCommandChannel,
                handleCommand = { appCommand ->
                    // processing command
                    val windowInfo = appCommand.toWindowInfo(openWindows.keys)
                    println("// processing command: $windowInfo")
                    openWindows += windowInfo.windowId to windowInfo
                },
            )
        }

        // render open windows
        openWindows.forEach()
        { openWindow ->
            val windowInfoId = openWindow.key
            val windowInfo = openWindow.value
            key(windowInfoId)
            {
                val windowInterface = object:WindowInterface
                {
                    override fun destroyWindow()
                    {
                        // remove the window from the open windows
                        openWindows -= windowInfoId

                        // if there are no more open windows, exit the application
                        if (openWindows.isEmpty())
                        {
                            exitApplication()
                        }
                    }

                    override var windowTitle: String
                        get() = windowInfo.title
                        set(value) { openWindows += windowInfoId to windowInfo.copy(title = value) }
                }

                Window(
                    onCloseRequest = { windowInterface.destroyWindow() },
                    title = windowInfo.title,
                )
                {
                    fillMaxBackground()
                    { themeColors ->
                        val params = WindowContentParams(
                            window = window,
                            windowInterface = windowInterface,
                            themeColors = themeColors,
                        )
                        windowInfo.content.content(params)
                    }
                }
            }
        }
    }

    private fun AppCommand.toWindowInfo(openWindows:Set<Long>):WindowInfo
    {
        val composeWindowContent = when (this)
        {
            AppCommand.OpenNewProjectWizard -> openNewProjectWizard()
            is AppCommand.OpenProject -> openOpenProject()
            AppCommand.OpenProjectBrowser -> openOpenProjectBrowser()
        }
        return WindowInfo(
            windowId = openWindows.maxOrNull()?.plus(1) ?: 0L,
            title = composeWindowContent.initialTitle,
            content = composeWindowContent,
        )
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
