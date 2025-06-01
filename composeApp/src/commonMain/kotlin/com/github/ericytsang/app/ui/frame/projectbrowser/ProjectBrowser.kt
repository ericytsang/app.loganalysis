package com.github.ericytsang.app.ui.frame.projectbrowser

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Colors
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.asset.IconSettings
import com.github.ericytsang.app.ui.frame.workingfileseteditor.ChildWindowManager
import com.github.ericytsang.app.ui.frame.workingfileseteditor.childWindowManager
import com.github.ericytsang.app.ui.frame.workingfileseteditor.showSettingsDialog
import kotlinx.coroutines.CoroutineScope

@Composable
fun ProjectBrowser(
    themeColors:Colors,
    rootChildWindowManager:ChildWindowManager,
    viewModelFactory: (CoroutineScope) -> ProjectBrowserViewModel,
)
{
    val coroutineScope = rememberCoroutineScope()
    val viewModel = remember { viewModelFactory(coroutineScope) }
    val items:List<ProjectListItemModel> by viewModel.getProjectsFlow.collectAsState(emptyList())
    val childWindowManager = childWindowManager()

    Column(
        modifier = Modifier.padding(Dimens.mttPadding)
    ) {
        // header with settings button
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

        // project list
        Surface(
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.small,
            border = ButtonDefaults.outlinedBorder,
        )
        {
            LazyColumn {
                items(
                    count = items.size,
                    key = { index -> items[index] },
                )
                { index ->
                    val itemScope = rememberCoroutineScope()
                    val modifier = if (index == 0)
                        Modifier.padding(Dimens.mttPadding)
                    else
                        Modifier.padding(bottom = Dimens.mttPadding).padding(horizontal = Dimens.mttPadding)
                    ProjectListItem(
                        item = items[index],
                        modifier = modifier,
                        projectBrowserViewModel = viewModel,
                        viewModelFactory = { scope,projectItem ->
                            ProjectListItemViewModelImpl(
                                uiScope = itemScope,
                                rootChildWindowManager = rootChildWindowManager,
                                project = projectItem,
                            )
                        },
                    )
                }
            }

        }
    }
}