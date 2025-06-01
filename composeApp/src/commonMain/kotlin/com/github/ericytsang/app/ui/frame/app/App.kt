package com.github.ericytsang.app.ui.frame.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.asset.IconEditLogFiles
import com.github.ericytsang.app.ui.asset.IconSettings
import com.github.ericytsang.app.ui.frame.projectbrowser.ProjectBrowser
import com.github.ericytsang.app.ui.frame.projectbrowser.ProjectBrowserViewModel
import com.github.ericytsang.app.ui.frame.workingfileseteditor.WorkingFileSetEditorViewModel
import com.github.ericytsang.app.ui.modal.openWorkingFileSetEditorInNewWindowBlocking
import com.github.ericytsang.app.usecase.ThemeUseCase
import com.github.ericytsang.app.util.fillMaxBackground
import kotlinx.coroutines.CoroutineScope

@Composable
fun App(
    window:ComposeWindow,
    themeUseCaseFactory:()->ThemeUseCase = { ThemeUseCase.instance },
    projectBrowserViewModelFactory:(CoroutineScope)->ProjectBrowserViewModel = { ProjectBrowserViewModel.create() },
    workingFileSetEditorViewModelFactory:()->WorkingFileSetEditorViewModel = { WorkingFileSetEditorViewModel.createDefault() },
)
{
    val workingFileSetEditorViewModel = remember { workingFileSetEditorViewModelFactory() }

    val themeUseCase = remember { themeUseCaseFactory() }
    fillMaxBackground()
    { themeColors ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        )
        {
            Row(
                modifier = Modifier.fillMaxWidth().padding(Dimens.mttPadding),
            ) {
                Button(
                    modifier = Modifier.padding(end = Dimens.mttPadding),
                    onClick =
                    {
                        openWorkingFileSetEditorInNewWindowBlocking(
                            owner = window,
                            viewModel = workingFileSetEditorViewModel,
                            themeUseCase = themeUseCase,
                        )
                    },
                    content = { IconEditLogFiles(themeColors.onPrimary) },
                )
                Button(
                    onClick = { /*openSettingsInNewWindowBlocking(window)*/ },
                    content = { IconSettings(themeColors.onPrimary) },
                )
            }
            ProjectBrowser(
                viewModelFactory = projectBrowserViewModelFactory,
                paddingValues = PaddingValues(
                    start = Dimens.mttPadding,
                    end = Dimens.mttPadding,
                    bottom = Dimens.mttPadding,
                )
            )
        }
    }
}
