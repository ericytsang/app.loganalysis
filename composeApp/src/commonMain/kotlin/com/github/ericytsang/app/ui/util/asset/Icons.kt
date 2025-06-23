package com.github.ericytsang.app.ui.util.asset

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.outline_delete_24
import kotlinproject.composeapp.generated.resources.outline_files_24
import kotlinproject.composeapp.generated.resources.outline_playlist_add_24
import kotlinproject.composeapp.generated.resources.outline_playlist_remove_24
import kotlinproject.composeapp.generated.resources.outline_settings_24
import kotlinproject.composeapp.generated.resources.outline_wrap_text_24
import org.jetbrains.compose.resources.painterResource

@Composable
fun IconWrapText(color:Color)
{
    Icon(
        painter = painterResource(Res.drawable.outline_wrap_text_24),
        contentDescription = "Wrap text",
        tint = color,
    )
}

@Composable
fun IconSettings(color:Color)
{
    Icon(
        painter = painterResource(Res.drawable.outline_settings_24),
        contentDescription = "Settings",
        tint = color,
    )
}

@Composable
fun IconDelete(color:Color)
{
    Icon(
        painter = painterResource(Res.drawable.outline_delete_24),
        contentDescription = "Delete",
        tint = color,
    )
}

@Composable
fun IconEditFileList(color:Color)
{
    Icon(
        painter = painterResource(Res.drawable.outline_files_24),
        contentDescription = "Edit File List",
        tint = color,
    )
}

@Composable
fun IconIncludeFilter(color:Color)
{
    Icon(
        painter = painterResource(Res.drawable.outline_playlist_add_24),
        contentDescription = "Include Filter",
        tint = color,
    )
}

@Composable
fun IconExcludeFilter(color:Color)
{
    Icon(
        painter = painterResource(Res.drawable.outline_playlist_remove_24),
        contentDescription = "Exclude Filter",
        tint = color,
    )
}

@Composable
fun IconDragHandle(color:Color)
{
    Icon(
        painter = painterResource(Res.drawable.outline_drag_handle_24),
        contentDescription = "Drag to reorder",
        tint = color,
    )
}
