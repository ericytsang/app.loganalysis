package com.github.ericytsang.app.ui.modal

import androidx.compose.runtime.MutableState
import com.github.ericytsang.app.ui.frame.workingFileSetEditor
import com.github.ericytsang.domain.objects.WorkingFileSet
import com.github.ericytsang.app.util.openBlockingDialog
import java.awt.Window

fun openWorkingFileSetEditorInNewWindowBlocking(
    owner:Window,
    workingFileSet:MutableState<WorkingFileSet>,
)
{
    openBlockingDialog(
        owner = owner,
        title = "Working File Set Editor",
        content = { workingFileSetEditor(owner = window,fileSet = workingFileSet) },
    )
}