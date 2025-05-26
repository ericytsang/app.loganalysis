package com.github.ericytsang.app.ui.frame.projectbrowser

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.util.animatedThemeColors
import com.github.ericytsang.app.util.fillMaxBackground
import com.github.ericytsang.domain.objects.Theme
import kotlinx.coroutines.CoroutineScope

@Composable
fun ProjectBrowser(
    viewModelFactory: (CoroutineScope) -> ProjectBrowserViewModel,
)
{
    val coroutineScope = rememberCoroutineScope()
    val viewModel = viewModelFactory(coroutineScope)
    val targetTheme by viewModel.theme.collectAsState(Theme.DARK)
    val themeColors by animatedThemeColors(targetTheme)
    val items:List<ProjectListItemModel> by viewModel.getProjectsFlow.collectAsState(emptyList())

    fillMaxBackground(colors = themeColors)
    {
        LazyColumn(
            modifier = Modifier.Companion.padding(Dimens.mttPadding)
        )
        {
            items(
                count = items.size,
                key = { index -> items[index] },
                itemContent =
                    { index ->
                        ProjectListItem(
                            item = items[index],
                            projectBrowserViewModel = viewModel,
                            viewModelFactory = { scope,projectItem -> ProjectListItemViewModelImpl(projectItem) },
                        )
                    },
            )
        }
    }
}