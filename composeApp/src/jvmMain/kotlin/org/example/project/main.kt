package org.example.project

import androidx.compose.material.Text
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.ericytsang.app.ui.frame.app.App
import com.github.ericytsang.app.util.EnsureSingletonProcessInstance

fun main()
{
    EnsureSingletonProcessInstance().acquireLock()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Log Analyzer",
        )
        {
            App(window = window)
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = "Log Viewer",

        )
        {
            Text("test")

            DialogWindow(
                onCloseRequest = { exitApplication() },
                title = "Log Viewer",
                resizable = true,
            )
            {
                Text("test2")
            }
        }
    }
}
