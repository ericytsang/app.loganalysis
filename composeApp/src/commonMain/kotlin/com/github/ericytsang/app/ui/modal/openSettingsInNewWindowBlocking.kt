package com.github.ericytsang.app.ui.modal

import com.github.ericytsang.app.app.Settings
import com.github.ericytsang.app.util.openBlockingDialog
import java.awt.Window

fun openSettingsInNewWindowBlocking(
    owner:Window,
)
{
    openBlockingDialog(
        owner = owner,
        title = "Settings",
    )
    {
        Settings()
    }
}