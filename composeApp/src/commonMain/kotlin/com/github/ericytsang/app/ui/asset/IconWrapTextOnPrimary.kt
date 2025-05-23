package com.github.ericytsang.app.ui.asset

import androidx.compose.foundation.Image
import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ColorFilter
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.outline_wrap_text_24
import org.jetbrains.compose.resources.painterResource

@Composable
fun IconWrapTextOnPrimary(animatedThemeColors:Colors)
{
    Image(
        painter = painterResource(Res.drawable.outline_wrap_text_24),
        contentDescription = "Wrap text",
        colorFilter = ColorFilter.Companion.lighting(
            multiply = animatedThemeColors.onPrimary,
            add = animatedThemeColors.onPrimary,
        )
    )
}