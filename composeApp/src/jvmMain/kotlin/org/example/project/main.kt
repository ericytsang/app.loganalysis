package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.ericytsang.app.ui.frame.app.App
import com.github.ericytsang.app.util.EnsureSingletonProcessInstance

fun main()
{
    EnsureSingletonProcessInstance().tryLock()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Log Analyzer",
        )
        {
            App(window = window)
        }
    }
}
