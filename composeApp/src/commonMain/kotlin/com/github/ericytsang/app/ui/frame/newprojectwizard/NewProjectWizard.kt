package com.github.ericytsang.app.ui.frame.newprojectwizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Colors
import androidx.compose.material.ContentAlpha
import androidx.compose.material.IconButton
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.asset.IconSettings
import com.github.ericytsang.app.ui.frame.commonwindowheader.CommonWindowHeader
import com.github.ericytsang.app.ui.frame.workingfileseteditor.childWindowManager
import com.github.ericytsang.app.ui.frame.workingfileseteditor.showSettingsDialog
import com.github.ericytsang.app.ui.modal.openMultiFilePicker

@Composable
fun NewProjectWizard(
    window:ComposeWindow,
    themeColors:Colors,
    viewModelFactory:()->NewProjectWizardViewModel = { NewProjectWizardViewModel.createDefault() },
)
{
    val viewModel = remember { viewModelFactory() }
    val selectedFiles by viewModel.selectedFiles.collectAsState(emptyList())

    fun openFilePickerToAddFiles()
    {
        openMultiFilePicker(window)
        { selectedFiles ->
            val selectedFiles = selectedFiles.map { SelectedFile(it.absolutePath) }
            viewModel.addFiles(selectedFiles)
        }
    }

    val childWindowManager = childWindowManager()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.mttPadding),
        horizontalAlignment = Alignment.Start,
    )
    {
        // header with settings button
        CommonWindowHeader(themeColors,childWindowManager)

        // spacer...
        Spacer(modifier = Modifier.size(Dimens.mttPadding))

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
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "No files selected",
                        fontStyle = FontStyle.Italic,
                        color = secondaryTextColor,
                    )
                }
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

        // spacer...
        Spacer(modifier = Modifier.size(Dimens.mttPadding))

        // footer with buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
        )
        {
            Button(
                onClick = { openFilePickerToAddFiles() },
                content = { Text("Add file(s)") },
            )

            Spacer(modifier = Modifier.weight(1f,fill = true))

            Spacer(modifier = Modifier.size(Dimens.mttPadding))

            Button(
                onClick = { openFilePickerToAddFiles() },
                colors = ButtonDefaults.buttonColors(themeColors.secondary),
                enabled = selectedFiles.isNotEmpty(),
                content = { Text("Done") },
            )
        }
    }
}