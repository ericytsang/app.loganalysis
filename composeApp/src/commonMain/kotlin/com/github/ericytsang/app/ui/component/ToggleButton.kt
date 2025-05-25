package com.github.ericytsang.app.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ToggleButton(
    modifier:Modifier,
    onClick:()->Unit,
    isToggled:Boolean,
    isNotToggledColors:ButtonColors = ButtonDefaults.buttonColors(),
    isToggledColors:ButtonColors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary),
    content:@Composable (contentColor:Color)->Unit,
)
{
    val targetButtonColors = if (isToggled) isToggledColors else isNotToggledColors
    val targetContentColor by targetButtonColors.contentColor(true)
    val contentColor by animateColorAsState(targetContentColor)
    val targetBackgroundColor by targetButtonColors.backgroundColor(true)
    val backgroundColor by animateColorAsState(targetBackgroundColor)
    val colors = ButtonDefaults.buttonColors(
        backgroundColor = backgroundColor,
        contentColor = contentColor,
    )
    Button(
        modifier = modifier,
        onClick = onClick,
        colors = colors,
        content = { content(contentColor) },
    )
}