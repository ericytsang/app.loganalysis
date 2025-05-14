package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowScope
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.Dialog
import java.awt.Dialog.ModalityType
import java.awt.Window
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.io.File
import javax.swing.JDialog

@Composable
@Preview
fun App(window:Window)
{
    var theme by remember { mutableStateOf(Theme.LIGHT) }
    val animatedThemeColors by animatedThemeColors(theme)

    val workingFileSetState = remember { mutableStateOf(WorkingFileSet(files = emptyList())) }
    val workingFileSet by workingFileSetState

    fillMaxBackground(colors = animatedThemeColors)
    {
        var showContent by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        )
        {
            Button(
                onClick = { showContent = !showContent },
                content = { Text("Click me!") },
            )

            Button(
                onClick = { theme = theme.getNextTheme() },
                content = { Text("Toggle theme") },
            )

            Button(
                onClick =
                {
                    openWorkingFileSetEditorInNewWindowBlocking(window,workingFileSetState)
                },
                content =
                {
                    if (workingFileSet.files.isEmpty())
                    {
                        Text("Add files")
                    }
                    else
                    {
                        Text("Edit files (${workingFileSet.files.size})")
                    }
                },
            )

            AnimatedVisibility(showContent)
            {
                val greeting = remember { Greeting().greet() }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
                {
                    Image(painterResource(Res.drawable.compose_multiplatform),null)
                    Text("Compose: $greeting")
                }
            }
        }
    }
}

data class WorkingFileSet(
    val files: List<File>,
)

object Dimens
{
    val minimumTouchTargetSize = 48.dp
    val minimumTouchTargetPadding = 8.dp
}

fun openWorkingFileSetEditorInNewWindowBlocking(
    owner:Window,
    workingFileSet:MutableState<WorkingFileSet>,
)
{
    openBlockingDialog(
        owner = owner,
        title = "Working File Set Editor",
        content = { workingFileSetEditor(owner = window, fileSet = workingFileSet) },
    )
}

fun openBlockingDialog(
    owner:Window,
    title:String,
    content:@Composable DialogWindowScope.() -> Unit,
)
{
    val dialog = ComposeDialog(owner = owner)
    dialog.setSize(800, 600)
    dialog.isModal = true
    dialog.setLocationRelativeTo(owner)
    dialog.title = title
    dialog.setContent { content() }
    dialog.defaultCloseOperation = JDialog.DISPOSE_ON_CLOSE
    dialog.isVisible = true
}

@Composable
fun workingFileSetEditor(
    owner:Dialog,
    fileSet: MutableState<WorkingFileSet>,
)
{
    var workingFileSet by fileSet

    fun openFilePickerToAddFiles()
    {
        openMultiFilePicker(owner)
        { selectedFiles ->
            workingFileSet = workingFileSet.copy(files = workingFileSet.files + selectedFiles)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.minimumTouchTargetPadding),
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
                Text(text = workingFileSet.files[index].absolutePath)
            }
        }
    }
}
