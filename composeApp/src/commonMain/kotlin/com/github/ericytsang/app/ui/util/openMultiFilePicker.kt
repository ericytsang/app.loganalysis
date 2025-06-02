package com.github.ericytsang.app.ui.util

import androidx.compose.ui.awt.ComposeWindow
import java.awt.FileDialog
import java.io.File

fun openMultiFilePicker(
    owner:ComposeWindow,
    onFilesSelected: (List<File>) -> Unit,
) {
    val fileDialog = FileDialog(owner,"Select file(s)",FileDialog.LOAD)
    fileDialog.isMultipleMode = true
    fileDialog.isVisible = true
    val selectedFiles = fileDialog.files.toList().filterNotNull()
    onFilesSelected(selectedFiles)
}