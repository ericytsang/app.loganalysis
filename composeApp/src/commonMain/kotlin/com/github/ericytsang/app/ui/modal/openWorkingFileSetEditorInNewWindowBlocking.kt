package com.github.ericytsang.app.ui.modal

import androidx.compose.ui.awt.ComposeWindow
import com.github.ericytsang.app.ui.frame.workingfileseteditor.WorkingFileSetEditor
import com.github.ericytsang.app.ui.frame.workingfileseteditor.WorkingFileSetEditorViewModel
import com.github.ericytsang.app.usecase.ThemeUseCase
import com.github.ericytsang.app.util.openBlockingDialog
import java.awt.Window

fun openWorkingFileSetEditorInNewWindowBlocking(
    owner:ComposeWindow,
    themeUseCase:ThemeUseCase,
    viewModel:WorkingFileSetEditorViewModel,
)
{
//    openBlockingDialog(
//        owner = owner,
//        title = "Working File Set Editor",
//    )
//    {
//        WorkingFileSetEditor(
//            owner = owner,
//            viewModel = viewModel,
//        )
//    }
}

