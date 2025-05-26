package com.github.ericytsang.app.ui.frame.projectbrowser

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.asset.IconDelete
import com.github.ericytsang.app.ui.component.ClickableSurface

@Composable
fun ProjectListItemProject(
    viewModel:ProjectListItemViewModel,
    themeColors:Colors,
)
{
    val textString by viewModel.name.collectAsState(initial = "Loading...")
    ClickableSurface(
        onClick = { viewModel.openProjectInLogViewer() },
        colors = ButtonDefaults.buttonColors(themeColors.surface),
    )
    {
        Row(
            modifier = Modifier.padding(Dimens.mttPadding),
        )
        {
            Text(
                text = textString,
                modifier = Modifier.padding(end = Dimens.mttPadding).weight(1f),
            )

            Button(
                onClick = { viewModel.deleteProject() },
                content = { IconDelete(themeColors.onPrimary) },
            )
        }
    }
}
