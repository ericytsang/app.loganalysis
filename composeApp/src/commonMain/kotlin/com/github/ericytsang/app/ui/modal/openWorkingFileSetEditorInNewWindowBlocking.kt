package com.github.ericytsang.app.ui.modal

import com.github.ericytsang.app.app.AppViewModel
import com.github.ericytsang.app.app.WorkingFileSetEditorViewModel
import com.github.ericytsang.app.ui.frame.workingFileSetEditor
import com.github.ericytsang.app.util.openBlockingDialog
import java.awt.Window

fun openWorkingFileSetEditorInNewWindowBlocking(
    owner:Window,
    appViewModel:AppViewModel,
    viewModel:WorkingFileSetEditorViewModel,
)
{
    openBlockingDialog(
        owner = owner,
        title = "Working File Set Editor",
    )
    {
        workingFileSetEditor(
            owner = window,
            viewModel = viewModel,
            appViewModel = appViewModel,
        )
    }
}
