package com.github.ericytsang.app.ui.util.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.ericytsang.app.ui.util.usecase.ThemeUseCase
import com.github.ericytsang.app.ui.util.animatedThemeColors
import com.github.ericytsang.domain.objects.Theme

/**
 * fills the entire area with the background color.
 */
@Composable
fun fillMaxBackground(
    themeUseCaseFactory:()->ThemeUseCase = { ThemeUseCase.instance },
    content:@Composable ColumnScope.(themeColors:Colors)->Unit,
)
{
    val themeUseCase = remember { themeUseCaseFactory() }
    val targetTheme by themeUseCase.theme.collectAsState(Theme.DARK)
    val themeColors by animatedThemeColors(targetTheme)
    MaterialTheme(colors = themeColors)
    {
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            content = { content(themeColors) },
        )
    }
}
