package com.github.ericytsang.app.ui.frame.newprojectwizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Colors
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.util.component.EmptyState
import com.github.ericytsang.app.ui.util.component.CommonWindowHeader
import com.github.ericytsang.app.ui.util.ChildWindowManager
import com.github.ericytsang.app.ui.util.ChildWindowManagerController
import com.github.ericytsang.app.ui.util.openMultiFilePicker

@Composable
fun NewProjectWizard(
    window:ComposeWindow,
    themeColors:Colors,
    rootChildWindowManager:ChildWindowManager,
    controller:ChildWindowManagerController,
    viewModelFactory:()->NewProjectWizardViewModel = {
        NewProjectWizardViewModel.create(
            rootChildWindowManager = rootChildWindowManager,
            controller = controller,
        )
    },
)
{
    val viewModel = remember { viewModelFactory() }
    val selectedFiles by viewModel.selectedFiles.collectAsState(emptyList())
    val isCreatingConfiguration by viewModel.isCreatingConfiguration.collectAsState(false)

    fun openFilePickerToAddFiles()
    {
        openMultiFilePicker(window)
        { selectedFiles ->
            val selectedFiles = selectedFiles.map { SelectedFile(it.absolutePath) }
            viewModel.addFiles(selectedFiles)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.mttPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.mttPadding),
        horizontalAlignment = Alignment.Start,
    )
    {
        // header with settings button
        CommonWindowHeader(themeColors,rootChildWindowManager)

        // region selected files list

        val commonPrefix = when (val firstFile = selectedFiles.firstOrNull())
        {
            null -> ""
            else -> selectedFiles.fold(firstFile.filePath) { acc,path -> acc.commonPrefixWith(path.filePath) }
        }

        val secondaryTextColor = themeColors.onBackground.copy(alpha = ContentAlpha.medium)

        Surface(
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.small,
            border = ButtonDefaults.outlinedBorder,
        )
        {
            if (selectedFiles.isEmpty())
            {
                EmptyState(
                    themeColors = themeColors,
                    text = "No files selected",
                )
            }
            else
            {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                )
                {
                    items(
                        count = selectedFiles.size,
                        key = { index -> selectedFiles[index].filePath },
                    )
                    { index ->
                        val filePath = selectedFiles[index].filePath
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(Dimens.mttPadding),
                        ) {
                            Text(
                                // if the texts have a common prefix, then make the common prefix portion the secondary text color
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(color = secondaryTextColor)) { append(commonPrefix) }
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(
                                            filePath.removePrefix(
                                                commonPrefix
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // endregion

        // footer with buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.mttPadding),
            modifier = Modifier.fillMaxWidth(),
        )
        {
            Spacer(modifier = Modifier.weight(1f,fill = true))

            val addFileButtonColors = ButtonDefaults.buttonColors(themeColors.surface)
            val addFileEnabled = !isCreatingConfiguration
            val addFileContentColor by addFileButtonColors.contentColor(true)
            Button(
                onClick = { openFilePickerToAddFiles() },
                content = { Text("Add file(s)", color = addFileContentColor) },
                colors = addFileButtonColors,
                enabled = addFileEnabled,
            )

            val doneButtonColors = ButtonDefaults.buttonColors(themeColors.primary)
            val doneButtonEnabled = !isCreatingConfiguration && selectedFiles.isNotEmpty()
            val doneButtonContentColor by doneButtonColors.contentColor(doneButtonEnabled)
            Button(
                onClick = { viewModel.onDoneButtonClicked() },
                colors = doneButtonColors,
                enabled = doneButtonEnabled,
                content = { Text("Done", color = doneButtonContentColor) },
            )
        }
    }
}
