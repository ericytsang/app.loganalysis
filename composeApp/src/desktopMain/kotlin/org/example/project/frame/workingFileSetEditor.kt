package org.example.project.frame

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
import kotlin.collections.plus
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.example.project.Dimens
import org.example.project.WorkingFileSet
import org.example.project.modal.openMultiFilePicker

@Composable
fun workingFileSetEditor(
    owner:Dialog,
    fileSet:MutableState<WorkingFileSet>,
)
{
    var workingFileSet by fileSet

    fun openFilePickerToAddFiles()
    {
        openMultiFilePicker(owner)
        { selectedFiles ->
            workingFileSet = workingFileSet.copy(files = workingFileSet.files+selectedFiles)
        }
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