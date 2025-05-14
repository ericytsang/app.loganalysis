package org.example.project.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
            modifier = Modifier.Companion
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            content = content,
        )
    }
}