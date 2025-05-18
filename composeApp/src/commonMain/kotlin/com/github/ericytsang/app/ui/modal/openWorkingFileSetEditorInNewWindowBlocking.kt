package com.github.ericytsang.app.ui.modal

import com.github.ericytsang.app.app.WorkingFileSetEditorViewModel
import com.github.ericytsang.app.ui.frame.workingFileSetEditor
import com.github.ericytsang.app.util.openBlockingDialog
import com.github.ericytsang.domain.objects.WorkingFileSet
import java.awt.Window
import java.io.File

fun openWorkingFileSetEditorInNewWindowBlocking(
    owner:Window,
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
            workingFileSet = viewModel.workingFileSet,
            addFilesToWorkingFileSet = { newFiles -> viewModel.addFiles(newFiles) },
        )
    }
}
