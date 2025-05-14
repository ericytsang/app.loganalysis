package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposeWindow
import java.awt.FileDialog
import java.io.File

@Composable
fun openMultiFilePicker(onFilesSelected: (List<File>) -> Unit) {
    val window = ComposeWindow()
    try
    {
        val fileDialog = FileDialog(window,"Select file(s)",FileDialog.LOAD)
        fileDialog.isMultipleMode = true
        fileDialog.isVisible = true
        val selectedFiles = fileDialog.files.toList().filterNotNull()
        onFilesSelected(selectedFiles)
    }
    finally
    {
        window.dispose()
    }
}