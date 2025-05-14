package org.example.project

import java.awt.Dialog
import java.awt.FileDialog
import java.io.File

fun openMultiFilePicker(
    owner:Dialog,
    onFilesSelected: (List<File>) -> Unit,
) {
    val fileDialog = FileDialog(owner,"Select file(s)",FileDialog.LOAD)
    fileDialog.isMultipleMode = true
    fileDialog.isVisible = true
    val selectedFiles = fileDialog.files.toList().filterNotNull()
    onFilesSelected(selectedFiles)
}