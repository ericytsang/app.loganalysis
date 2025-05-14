package com.github.ericytsang.app.util

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.graphics.Color
import com.github.ericytsang.app.model.Theme

/**
 * animates the colors of the theme.
 * @param theme the theme to use for the colors.
 * @return the animated colors of the theme.
 */
@Composable
fun animatedThemeColors(theme:Theme):State<Colors>
{
    val themeColors = when (theme)
    {
        Theme.LIGHT -> lightColors()
        Theme.DARK -> darkColors()
    }

    val animatedThemeColors = animateColorsAsState(
        targetValue = themeColors,
        animationSpec = spring<Color>(stiffness = Spring.StiffnessLow)
    )

    return animatedThemeColors
}

/**
 * animates the colors of the theme.
 * @param targetValue the target value of the colors.
 * @param animationSpec the animation spec to use for the animation.
 * @param label the label to use for the animation.
 */
@Composable
private fun animateColorsAsState(
    targetValue:Colors,
    animationSpec:AnimationSpec<Color> = spring<Color>(),
    label: String = "ColorAnimation",
):State<Colors>
{
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