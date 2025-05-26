package com.github.ericytsang.app.ui.frame.workingfileseteditor

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
import com.github.ericytsang.app.ui.frame.app.AppViewModel
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.modal.openMultiFilePicker
import com.github.ericytsang.app.util.animatedThemeColors
import com.github.ericytsang.app.util.fillMaxBackground
import com.github.ericytsang.domain.objects.Theme
import com.github.ericytsang.domain.objects.WorkingFileSetEmpty
import java.awt.Dialog

@Composable
fun WorkingFileSetEditor(
    owner:Dialog,
    viewModel:WorkingFileSetEditorViewModel,
    appViewModel:AppViewModel,
)
{
    val theme by appViewModel.theme.collectAsState(Theme.DARK)
    val animatedThemeColors by animatedThemeColors(theme)

    val workingFileSet by viewModel.workingFileSet.collectAsState(WorkingFileSetEmpty)

    fun openFilePickerToAddFiles()
    {
        openMultiFilePicker(owner) { selectedFiles -> viewModel.addFiles(selectedFiles) }
    }

    fillMaxBackground(colors = animatedThemeColors)
    {
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(Dimens.mttPadding),
            horizontalAlignment = Alignment.Start,
        )
        {
            Button(
                onClick = { openFilePickerToAddFiles() },
                content = { Text("Add file(s)") },
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            )
            {
                items(count = workingFileSet.files.size)
                { index ->
                    Text(text = workingFileSet.files[index].filePath)
                }
            }
        }
    }
}
