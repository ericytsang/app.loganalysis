package com.github.ericytsang.app.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.DialogWindowScope
import java.awt.Window
import javax.swing.JDialog

@Composable
fun openBlockingDialog(
    onCloseRequest: () -> Unit,
    title:String,
    content:@Composable DialogWindowScope.() -> Unit,
)
{
    DialogWindow(
        onCloseRequest = onCloseRequest,
        title = title,
        content = content,
    )
}

fun openNonBlockingDialog(
    owner:Window,
    title:String,
    content:@Composable DialogWindowScope.() -> Unit,
)
{
    val dialog = ComposeDialog(owner = owner)
    dialog.setSize(800, 600)
    dialog.setLocationRelativeTo(owner)
    dialog.title = title
    dialog.setContent { content() }
    dialog.defaultCloseOperation = JDialog.DISPOSE_ON_CLOSE
    dialog.isVisible = true
}
