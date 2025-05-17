package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.ericytsang.app.app.App
import com.github.ericytsang.app.app.ColdStartDependencyProviderImpl

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Log Analyzer",
    )
    {
        App(
            dependencyProvider = ColdStartDependencyProviderImpl,
            window = window,
        )
    }
}
