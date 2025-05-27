package com.github.ericytsang.app.ui.frame.projectbrowser

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.github.ericytsang.app.model.Dimens
import kotlinx.coroutines.CoroutineScope

@Composable
fun ProjectBrowser(
    viewModelFactory: (CoroutineScope) -> ProjectBrowserViewModel,
    paddingValues:PaddingValues = PaddingValues(Dimens.mttPadding),
)
{
    val coroutineScope = rememberCoroutineScope()
    val viewModel = remember { viewModelFactory(coroutineScope) }
    val items:List<ProjectListItemModel> by viewModel.getProjectsFlow.collectAsState(emptyList())

    LazyColumn(
        modifier = Modifier.padding(paddingValues)
    )
    {
        items(
            count = items.size,
            key = { index -> items[index] },
            itemContent =
            { index ->
                val itemScope = rememberCoroutineScope()
                ProjectListItem(
                    item = items[index],
                    projectBrowserViewModel = viewModel,
                    viewModelFactory = { scope,projectItem -> ProjectListItemViewModelImpl(itemScope, projectItem) },
                )
            },
        )
    }
}