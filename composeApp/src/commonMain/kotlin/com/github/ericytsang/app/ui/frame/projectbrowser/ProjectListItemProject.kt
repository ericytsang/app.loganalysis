package com.github.ericytsang.app.ui.frame.projectbrowser

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.asset.IconDelete
import com.github.ericytsang.app.ui.asset.IconEdit
import com.github.ericytsang.app.ui.asset.IconOpen

@Composable
fun ProjectListItemProject(
    viewModel:ProjectListItemViewModel,
    themeColors:Colors,
)
{
    val textString by viewModel.name.collectAsState(initial = "Loading...")
    Row(
        modifier = Modifier.Companion.padding(Dimens.mttPadding),
    )
    {
        Text(
            text = textString,
            modifier = Modifier.Companion.padding(end = Dimens.mttPadding).weight(1f),
        )

        Button(
            onClick = { viewModel.deleteProject() },
            content = { IconDelete(themeColors.onPrimary) },
        )

        Button(
            onClick = { viewModel.openWorkingFileSetEditor() },
            content = { IconEdit(themeColors.onPrimary) },
        )

        Button(
            onClick = { viewModel.openProjectInLogViewer() },
            content = { IconOpen(themeColors.onPrimary) },
        )
    }
}