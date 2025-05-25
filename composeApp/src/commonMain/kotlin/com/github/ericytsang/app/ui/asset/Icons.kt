package com.github.ericytsang.app.ui.asset

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.outline_contract_edit_24
import kotlinproject.composeapp.generated.resources.outline_match_case_24
import kotlinproject.composeapp.generated.resources.outline_settings_24
import kotlinproject.composeapp.generated.resources.outline_wrap_text_24
import org.jetbrains.compose.resources.painterResource

@Composable
fun IconEditLogFilesOnPrimary(color:Color)
{
    Image(
        painter = painterResource(Res.drawable.outline_contract_edit_24),
        contentDescription = "Edit log files",
        colorFilter = ColorFilter.Companion.lighting(
            multiply = color,
            add = color,
        )
    )
}

@Composable
fun IconWrapTextOnPrimary(color:Color)
{
    Image(
        painter = painterResource(Res.drawable.outline_wrap_text_24),
        contentDescription = "Wrap text",
        colorFilter = ColorFilter.Companion.lighting(
            multiply = color,
            add = color,
        )
    )
}

@Composable
fun IconMatchCaseOnPrimary(color:Color)
{
    Image(
        painter = painterResource(Res.drawable.outline_match_case_24),
        contentDescription = "Match case",
        colorFilter = ColorFilter.Companion.lighting(
            multiply = color,
            add = color,
        )
    )
}

@Composable
fun IconSettingsOnPrimary(color:Color)
{
    Image(
        painter = painterResource(Res.drawable.outline_settings_24),
        contentDescription = "Settings",
        colorFilter = ColorFilter.Companion.lighting(
            multiply = color,
            add = color,
        )
    )
}
