package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.compose_multiplatform

enum class Theme
{
    LIGHT,
    DARK,
}

@Composable
@Preview
fun App()
{
    var theme by remember { mutableStateOf(Theme.LIGHT) }

    val themeColors = when (theme)
    {
        Theme.LIGHT -> lightColors()
        Theme.DARK -> darkColors()
    }

    fillMaxBackground(colors = themeColors)
    {
        var showContent by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        )
        {
            Button(
                onClick = { showContent = !showContent },
            )
            {
                Text("Click me!")
            }

            Button(
                onClick =
                    {
                        theme = when (theme)
                        {
                            Theme.LIGHT -> Theme.DARK
                            Theme.DARK -> Theme.LIGHT
                        }
                    },
            )
            {
                Text("Toggle theme")
            }

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

@Composable
fun fillMaxBackground(
    colors:Colors,
    content:@Composable ColumnScope.()->Unit,
)
{
    MaterialTheme(colors = colors)
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            content = content,
        )
    }
}
