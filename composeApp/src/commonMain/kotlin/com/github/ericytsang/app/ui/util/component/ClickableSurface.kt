package com.github.ericytsang.app.ui.util.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClickableSurface(
    onClick: () -> Unit,
    modifier:Modifier = Modifier.Companion,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    elevation: ButtonElevation? = ButtonDefaults.elevation(),
    shape:Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    colors:ButtonColors = ButtonDefaults.buttonColors(),
    content: @Composable () -> Unit
)
{
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val contentColor by colors.contentColor(enabled)
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { role = Role.Button },
        enabled = enabled,
        shape = shape,
        color = colors.backgroundColor(enabled).value,
        contentColor = contentColor.copy(alpha = 1f),
        border = border,
        elevation = elevation?.elevation(enabled,interactionSource)?.value ?: 0.dp,
        interactionSource = interactionSource,
        content = { content() },
    )
}