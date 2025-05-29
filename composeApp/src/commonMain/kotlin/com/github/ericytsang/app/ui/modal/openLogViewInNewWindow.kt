package com.github.ericytsang.app.ui.modal

import androidx.compose.ui.awt.ComposeWindow
import com.github.ericytsang.app.ui.frame.logviewer.LogViewerWindow
import com.github.ericytsang.app.util.openNonBlockingDialog
import java.awt.Window

fun openLogViewInNewWindow(
    owner:ComposeWindow,
)
{
    openNonBlockingDialog(
        owner = owner,
        title = "Log Viewer",
    )
    {
        LogViewerWindow(window = owner)
    }
}