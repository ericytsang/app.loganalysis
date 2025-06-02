package com.github.ericytsang.app.ui.util.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Colors
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.util.asset.IconSettings
import com.github.ericytsang.app.ui.util.ChildWindowManager
import com.github.ericytsang.app.ui.util.openSettingsDialog
import kotlin.system.exitProcess

@Composable
fun CommonWindowHeader(
    themeColors:Colors,
    childWindowManager:ChildWindowManager,
    content: @Composable (() -> Unit) = {}
)
{
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.mttPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        // custom content
        content()

        // exit button
        Button(
            onClick = { exitProcess(0) },
            content = { Text("Exit app") },
            colors = ButtonDefaults.buttonColors(themeColors.error),
        )

        // move the icon button the end
        Spacer(modifier = Modifier.weight(1f,fill = true))

        // settings button
        IconButton(
            modifier = Modifier.padding(end = Dimens.mttPadding),
            onClick = { childWindowManager.openSettingsDialog() },
            content = { IconSettings(themeColors.onSurface) },
        )
    }
}
