package org.example.project.modal

import androidx.compose.runtime.MutableState
import org.example.project.WorkingFileSet
import org.example.project.frame.workingFileSetEditor
import org.example.project.util.openBlockingDialog
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