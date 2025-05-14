package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview


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

    val animatedThemeColors = animateColorsAsState(
        targetValue = themeColors,
        animationSpec = spring<Color>(stiffness = Spring.StiffnessLow)
    )

    fillMaxBackground(colors = animatedThemeColors.value)
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

