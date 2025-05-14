package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
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

/**
 * supported themes.
 */
enum class Theme
{
    LIGHT,
    DARK,
}

/**
 * animates the colors of the theme.
 * @param targetValue the target value of the colors.
 * @param animationSpec the animation spec to use for the animation.
 * @param label the label to use for the animation.
 */
@Composable
fun animateColorsAsState(
    targetValue: Colors,
    animationSpec: AnimationSpec<Color> = spring<Color>(),
    label: String = "ColorAnimation",
): State<Colors> {
    val primary = animateColorAsState(targetValue.primary,animationSpec,label)
    val primaryVariant = animateColorAsState(targetValue.primaryVariant,animationSpec,label)
    val secondary = animateColorAsState(targetValue.secondary,animationSpec,label)
    val secondaryVariant = animateColorAsState(targetValue.secondaryVariant,animationSpec,label)
    val background = animateColorAsState(targetValue.background,animationSpec,label)
    val surface = animateColorAsState(targetValue.surface,animationSpec,label)
    val error = animateColorAsState(targetValue.error,animationSpec,label)
    val onPrimary = animateColorAsState(targetValue.onPrimary,animationSpec,label)
    val onSecondary = animateColorAsState(targetValue.onSecondary,animationSpec,label)
    val onBackground = animateColorAsState(targetValue.onBackground,animationSpec,label)
    val onSurface = animateColorAsState(targetValue.onSurface,animationSpec,label)
    val onError = animateColorAsState(targetValue.onError,animationSpec,label)
    return derivedStateOf()
    {
        Colors(
            primary = primary.value,
            primaryVariant = primaryVariant.value,
            secondary = secondary.value,
            secondaryVariant = secondaryVariant.value,
            background = background.value,
            surface = surface.value,
            error = error.value,
            onPrimary = onPrimary.value,
            onSecondary = onSecondary.value,
            onBackground = onBackground.value,
            onSurface = onSurface.value,
            onError = onError.value,
            isLight = targetValue.isLight,
        )
    }
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

/**
 * fills the entire area with the background color.
 */
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
