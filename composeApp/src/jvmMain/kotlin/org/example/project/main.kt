package org.example.project

import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
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
import com.github.ericytsang.app.ui.frame.app.NewProjectWizardWindow
import com.github.ericytsang.app.ui.frame.app.WindowContentParams
import com.github.ericytsang.app.ui.frame.app.WindowInfo
import com.github.ericytsang.app.ui.frame.app.WindowInterface
import com.github.ericytsang.app.ui.frame.app.openOpenProject
import com.github.ericytsang.app.ui.frame.app.openOpenProjectBrowser
import com.github.ericytsang.app.ui.frame.workingfileseteditor.ChildWindowManager
import com.github.ericytsang.app.ui.frame.workingfileseteditor.childWindowManager
import com.github.ericytsang.app.util.EnsureSingletonProcessInstance
import com.github.ericytsang.app.util.fillMaxBackground
import com.github.ericytsang.kotlin.ImmutableCoroutineScope.Companion.asImmutableCoroutineScope
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.channels.Channel
import kotlin.collections.minus
import kotlin.collections.plus

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
                NewProjectWizardWindow(childWindowManager,controller)
            }
            is AppCommand.OpenProject -> childWindowManager.addChildWindow { controller ->
                NewProjectWizardWindow(childWindowManager,controller)
            }
            AppCommand.OpenProjectBrowser -> childWindowManager.addChildWindow { controller ->
                NewProjectWizardWindow(childWindowManager,controller)
            }
        }
    }

    private fun createWindowInterface(
        openWindowsState:MutableState<Map<Long,WindowInfo>>,
        windowInfo:WindowInfo,
        exitApplication:()->Unit,
    ):WindowInterface = object:WindowInterface
    {
        val windowInfoId = windowInfo.windowId
        var openWindows by openWindowsState

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

        override var windowTitle:String
            get() = windowInfo.title
            set(value)
            {
                openWindows += windowInfoId to windowInfo.copy(title = value)
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
