package com.github.ericytsang.app.ui.frame.projectbrowser

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.usecase.ThemeUseCase
import com.github.ericytsang.app.util.animatedThemeColors
import com.github.ericytsang.domain.objects.Theme
import kotlinx.coroutines.CoroutineScope

@Composable
fun ProjectListItem(
    item:ProjectListItemModel,
    projectBrowserViewModel:ProjectBrowserViewModel,
    themeUseCase:ThemeUseCase = ThemeUseCase.instance,
    viewModelFactory: (CoroutineScope,ProjectListItemModel.Project) -> ProjectListItemViewModel,
)
{
    val targetTheme by themeUseCase.theme.collectAsState(Theme.DARK)
    val themeColors by animatedThemeColors(targetTheme)
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = Modifier.padding(bottom = Dimens.mttPadding))
    {
        when (item)
        {
            is ProjectListItemModel.Project ->
            {
                val itemViewModel = remember { viewModelFactory(coroutineScope,item) }
                ProjectListItemProject(
                    viewModel = itemViewModel,
                    themeColors = themeColors
                )
            }

            is ProjectListItemModel.LazyLoadMoreItemsBelow ->
            {
                projectBrowserViewModel.loadMoreItems(item)
                Text(
                    text = "Loading more items...",
                    modifier = Modifier.Companion.padding(Dimens.mttPadding),
                )
            }

            is ProjectListItemModel.LazyLoadMoreItemsAbove ->
            {
                projectBrowserViewModel.loadMoreItems(item)
                Text(
                    text = "Loading more items...",
                    modifier = Modifier.Companion.padding(Dimens.mttPadding),
                )
            }
        }
    }
}