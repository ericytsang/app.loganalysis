package com.github.ericytsang.app.ui.frame.projectbrowser

import androidx.compose.foundation.layout.Row
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
import com.github.ericytsang.app.ui.util.asset.IconDelete
import com.github.ericytsang.app.ui.util.component.ClickableSurface
import com.github.ericytsang.app.ui.frame.projectbrowser.ProjectListItemViewModelImpl.ProjectItemTexts

@Composable
fun ProjectListItemProject(
    viewModel:ProjectListItemViewModel,
    themeColors:Colors,
)
{
    val textString by viewModel.name.collectAsState(initial = ProjectItemTexts("Loading..."))
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
                text = textString.toAnnotatedString(themeColors),
                modifier = Modifier.padding(end = Dimens.mttPadding).weight(1f),
            )

            val buttonColors = ButtonDefaults.buttonColors(themeColors.error)
            val contentColor by buttonColors.contentColor(true)
            Button(
                onClick = { viewModel.deleteProject() },
                content = { IconDelete(contentColor) },
                colors = buttonColors,
            )
        }
    }
}
