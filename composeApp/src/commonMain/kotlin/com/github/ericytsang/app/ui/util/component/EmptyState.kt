package com.github.ericytsang.app.ui.util.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Colors
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle

@Composable
fun EmptyState(
    themeColors:Colors,
    text:String,
)
{
    val secondaryTextColor = themeColors.onBackground.copy(alpha = ContentAlpha.medium)
    Column(
        modifier = Modifier.Companion.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
    )
    {
        Text(
            text = text,
            fontStyle = FontStyle.Companion.Italic,
            color = secondaryTextColor,
        )
    }
}