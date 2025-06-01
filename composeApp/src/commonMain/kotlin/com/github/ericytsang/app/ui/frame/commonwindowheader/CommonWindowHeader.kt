package com.github.ericytsang.app.ui.frame.commonwindowheader

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Colors
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.asset.IconSettings
import com.github.ericytsang.app.ui.frame.workingfileseteditor.ChildWindowManager
import com.github.ericytsang.app.ui.frame.workingfileseteditor.showSettingsDialog

@Composable
fun CommonWindowHeader(
    themeColors:Colors,
    childWindowManager:ChildWindowManager,
)
{
    Row {

        // move the icon button the end
        Spacer(modifier = Modifier.weight(1f,fill = true))

        // settings button
        IconButton(
            modifier = Modifier.padding(end = Dimens.mttPadding),
            onClick = { childWindowManager.showSettingsDialog() },
            content = { IconSettings(themeColors.onSurface) },
        )
    }
}
