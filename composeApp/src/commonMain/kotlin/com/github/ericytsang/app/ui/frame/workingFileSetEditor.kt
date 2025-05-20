package com.github.ericytsang.app.ui.frame

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.ericytsang.app.app.WorkingFileSetEditorViewModel
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.modal.openMultiFilePicker
import com.github.ericytsang.domain.objects.WorkingFileSetEmpty
import java.awt.Dialog

@Composable
fun workingFileSetEditor(
    owner:Dialog,
    viewModel:WorkingFileSetEditorViewModel,
)
{
    val workingFileSet by viewModel.workingFileSet.collectAsState(WorkingFileSetEmpty)

    fun openFilePickerToAddFiles()
    {
        openMultiFilePicker(owner) { selectedFiles -> viewModel.addFiles(selectedFiles) }
    }

    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(Dimens.mttPadding),
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
                Text(text = workingFileSet.files[index].filePath)
            }
        }
    }
}
