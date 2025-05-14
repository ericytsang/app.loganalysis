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
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App()
{
    var theme by remember { mutableStateOf(Theme.LIGHT) }
    val animatedThemeColors by animatedThemeColors(theme)
    var requestOpenFilePicker by remember { mutableStateOf(0) }
    if (requestOpenFilePicker > 0)
    {
        requestOpenFilePicker -= 1
        openMultiFilePicker {
            println(it.joinToString("\n") { file -> file.absolutePath })
        }
    }

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
                onClick = { requestOpenFilePicker += 1 },
                content = { Text("Open files") },
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

