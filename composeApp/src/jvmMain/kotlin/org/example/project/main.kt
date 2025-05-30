package org.example.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.time.Duration.Companion.seconds

class Main(
    kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
):KotlinDependencyProvider by kotlinDependencyProvider
{

    private val appCommandChannel = Channel<AppCommand>(capacity = Channel.UNLIMITED)
    private val appCommandChannelFlow get() = appCommandChannel.receiveAsFlow()

    private val doneLoadingSignalChannel = Channel<Unit>(capacity = Channel.UNLIMITED)
    private val doneLoadingSignalFlow get() = doneLoadingSignalChannel.receiveAsFlow()

    fun main()
    {
        // asynchronously initialize the application
        applicationScope.launch(dispatchers.io)
        {
            delay(20.seconds)
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

        // show a loading dialog right away
        var showDialogState by mutableStateOf(true)
        DialogWindow(
            onCloseRequest = { exitApplication() },
            title = "Log Viewer",
            alwaysOnTop = true,
            resizable = false,
            visible = showDialogState,
            content = { fillMaxBackground { themeColors -> LoadingText() } },
        )

        // close the loading dialog when the application is done loading
        val doneLoadingSignal by doneLoadingSignalFlow.collectAsState(null)
        if (doneLoadingSignal != null)
        {
            showDialogState = false
        }

        // keep track of all open windows
        var openWindows by mutableStateOf<Map<Long, WindowInfo>>(emptyMap())

        // consume and handle view model requests
        val appCommand by appCommandChannelFlow.collectAsState(null)
        appCommand?.toWindowInfo(openWindows.keys)?.also { windowInfo ->
            openWindows += windowInfo.windowId to windowInfo
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
                        openWindows -= windowInfoId
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
