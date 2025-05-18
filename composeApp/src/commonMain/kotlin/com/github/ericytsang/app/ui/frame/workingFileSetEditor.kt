package com.github.ericytsang.app.ui.frame

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.awt.Dialog
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.modal.openMultiFilePicker
import com.github.ericytsang.domain.objects.WorkingFileSet
import java.io.File

@Composable
fun workingFileSetEditor(
    owner:Dialog,
    workingFileSet:WorkingFileSet,
    addFilesToWorkingFileSet:(List<File>) -> Unit,
)
{
    fun openFilePickerToAddFiles()
    {
        openMultiFilePicker(owner) { selectedFiles -> addFilesToWorkingFileSet(selectedFiles) }
    }

    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(Dimens.minimumTouchTargetPadding),
        horizontalAlignment = Alignment.Companion.Start,
    )
    {
        Button(
            onClick = { openFilePickerToAddFiles() },
            content = { Text("Add file(s)") },
        )

        LazyColumn(
            modifier = Modifier.Companion.fillMaxSize(),
        )
        {
            items(count = workingFileSet.files.size)
            { index ->
                Text(text = workingFileSet.files[index].absolutePath)
            }
        }
    }
}