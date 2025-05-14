package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.compose_multiplatform
import org.example.project.domain.WorkingFileSet
import org.example.project.modal.openWorkingFileSetEditorInNewWindowBlocking
import org.example.project.util.animatedThemeColors
import org.example.project.util.fillMaxBackground
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.Window

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

object Dimens
{
    val minimumTouchTargetSize = 48.dp
    val minimumTouchTargetPadding = 8.dp
}

