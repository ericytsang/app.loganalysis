package com.github.ericytsang.app.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.ericytsang.app.ui.modal.openWorkingFileSetEditorInNewWindowBlocking
import com.github.ericytsang.app.util.animatedThemeColors
import com.github.ericytsang.app.util.fillMaxBackground
import com.github.ericytsang.domain.objects.Theme
import com.github.ericytsang.domain.objects.WorkingFileSet
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.Window

@Composable
@Preview
fun App(
    window:Window,
    viewModel:AppViewModel = AppViewModel.create(),
    workingFileSetEditorViewModel:WorkingFileSetEditorViewModel = WorkingFileSetEditorViewModel.createDefault(),
)
{
    val theme by viewModel.theme.collectAsState(Theme.DARK)
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
                onClick = { viewModel.switchTheme() },
                content = { Text("Toggle theme") },
            )

            Button(
                onClick =
                {
                    openWorkingFileSetEditorInNewWindowBlocking(
                        owner = window,
                        viewModel = workingFileSetEditorViewModel,
                    )
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

