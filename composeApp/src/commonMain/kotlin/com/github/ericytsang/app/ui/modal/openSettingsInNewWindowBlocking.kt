package com.github.ericytsang.app.ui.modal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.ericytsang.app.ui.frame.settings.Settings
import com.github.ericytsang.app.util.openBlockingDialog
import java.awt.Window

@Composable
fun openSettingsInNewWindowBlocking()
{
    var showDialog by remember { mutableStateOf(true) }
    if (showDialog)
    {
        openBlockingDialog(
            onCloseRequest = { showDialog = false },
            title = "Settings",
            content = { Settings() },
        )
    }
}

@Composable
fun SettingsDialog(
    onCloseRequest:()->Unit,
)
{
    openBlockingDialog(
        onCloseRequest = onCloseRequest,
        title = "Settings",
        content = { Settings() },
    )
}