@file:OptIn(ExperimentalMaterialApi::class)

package com.github.ericytsang.app.ui.frame.logviewer

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Colors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.frame.workingfileseteditor.LogFileListViewModel
import com.github.ericytsang.app.ui.frame.workingfileseteditor.WorkingFileSetEditorViewModel
import com.github.ericytsang.app.ui.frame.workingfileseteditor.collapsableEditWorkingFilesSection
import com.github.ericytsang.app.ui.util.ChildWindowManager
import com.github.ericytsang.app.ui.util.asset.IconEditFileList
import com.github.ericytsang.app.ui.util.asset.IconExcludeFilter
import com.github.ericytsang.app.ui.util.asset.IconIncludeFilter
import com.github.ericytsang.app.ui.util.asset.IconWrapText
import com.github.ericytsang.app.ui.util.component.ColorCodedLogLine
import com.github.ericytsang.app.ui.util.component.CommonWindowHeader
import com.github.ericytsang.app.ui.util.component.ToggleButton
import com.github.ericytsang.app.ui.util.openNewProjectWizard
import com.github.ericytsang.app.ui.util.openProjectBrowser
import com.github.ericytsang.domain.objects.FilePath.Companion.toFile
import com.github.ericytsang.domain.objects.FilterId
import com.github.ericytsang.domain.objects.FilterType
import com.github.ericytsang.domain.objects.Theme
import com.github.ericytsang.domain.objects.WorkingFileSetEmpty
import kotlinx.coroutines.CoroutineScope
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun LogViewerRoot(
    themeColors:Colors,
    rootChildWindowManager:ChildWindowManager,
    logFileListViewModelFactory:()->LogFileListViewModel,
    filterSetViewModelFactory:(FilterType)->FilterSetViewModel,
    workingFileSetEditorViewModelFactory:()->WorkingFileSetEditorViewModel,
    logViewerViewModelFactory:()->LogViewerViewModel,
    reorderSidebarItemViewModelFactory:()->ReorderSidebarItemViewModel,
    viewModelFactory:(CoroutineScope)->LogViewerRootViewModel = { uiScope -> LogViewerRootViewModel.create(uiScope) },
)
{
    val uiScope = rememberCoroutineScope()
    val viewModel = remember { viewModelFactory(uiScope) }
    val logFileListViewModel = remember { logFileListViewModelFactory() }
    val includeFilterSetViewModel = remember { filterSetViewModelFactory(FilterType.INCLUDE) }
    val excludeFilterSetViewModel = remember { filterSetViewModelFactory(FilterType.EXCLUDE) }
    val logViewerViewModel = remember { logViewerViewModelFactory() }
    val reorderSidebarItemViewModel = remember { reorderSidebarItemViewModelFactory() }
    val workingFileSetEditorViewModel = remember { workingFileSetEditorViewModelFactory() }

    val theme by viewModel.theme.collectAsState(Theme.DARK)

    val delimiters by viewModel.getDelimiterFlow().collectAsState("")

    val workingFileSet by workingFileSetEditorViewModel.workingFileSet.collectAsState(WorkingFileSetEmpty)
    logViewerViewModel.setConcatenatedFiles(workingFileSet.files.map { it.filePath.toFile() })

    val shouldWrapText by viewModel.getWordWrapFlow().collectAsState(false)

    Column(
        modifier = Modifier.fillMaxSize().padding(Dimens.mttPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.mttPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    )
    {
        CommonWindowHeader(themeColors,rootChildWindowManager)
        {
            // new project button
            Button(
                onClick = { rootChildWindowManager.openNewProjectWizard() },
                content = { Text("New project") },
                colors = ButtonDefaults.buttonColors(themeColors.surface),
            )

            // open project browser button
            Button(
                onClick = { rootChildWindowManager.openProjectBrowser() },
                content = { Text("Recent projects") },
                colors = ButtonDefaults.buttonColors(themeColors.surface),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.mttPadding),
        )
        {
            // region log viewer

            val logLines by logViewerViewModel.getLogLinesFlow().collectAsState(emptyList())

            Surface(
                modifier = Modifier.weight(1f,fill = true),
                shape = MaterialTheme.shapes.small,
                border = ButtonDefaults.outlinedBorder,
            )
            {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                )
                {
                    items(
                        key = { index -> logLines[index].key },
                        count = logLines.size,
                    )
                    { index ->
                        Row(modifier = Modifier.animateItem())
                        {
                            ColorCodedLogLine(
                                text = logLines[index].line,
                                modifier = Modifier.fillMaxWidth(),
                                delimiters = delimiters,
                                themeForColorCoding = theme,
                                defaultColor = themeColors.onBackground,
                                softWrap = shouldWrapText,
                            )
                        }
                    }
                }
            }

            // endregion

            // region filter helpers and working file set editor

            var showEditFileListPanel by remember { mutableStateOf(false) }
            var showExcludeFilterPanel by remember { mutableStateOf(false) }
            var showIncludeFilterPanel by remember { mutableStateOf(false) }

            var filterIdOfSelectedFilterEditorPanel by remember { mutableStateOf<FilterId?>(null) }

            val isSidebarExpanded by derivedStateOf { showEditFileListPanel || showExcludeFilterPanel || showIncludeFilterPanel }

            val columnWidth by derivedStateOf { if (isSidebarExpanded) 400.dp else Dimens.mttPadding*3+Dimens.mttSize }

            Surface(
                modifier = Modifier.fillMaxHeight(),
                shape = MaterialTheme.shapes.small,
                border = ButtonDefaults.outlinedBorder,
            )
            {
                val stateForLazyListOfIncludeFilters = rememberLazyListState()
                val reorderableLazyListStateForLazyListOfIncludeFilters = rememberReorderableLazyListState(stateForLazyListOfIncludeFilters)
                { from,to ->
                    // fyi, the key is of type ReorderableSidebarItemKey, which is a sealed interface
                    println("reorder from ${from.key} to ${to.key}")
                    val fromKey = from.key as? ReorderableSidebarItemKey ?: error("unexpected from key type: ${from.key}")
                    val toKey = to.key as? ReorderableSidebarItemKey ?: error("unexpected to key type: ${to.key}")
                    reorderSidebarItemViewModel.moveItem(fromKey,toKey)
                }
                val workingFileItemViewModels by logFileListViewModel.itemViewModels.collectAsState(emptyList())
                val excludeFilterItemViewModels by excludeFilterSetViewModel.filters.collectAsState(emptyList())
                val includeFilterItemViewModels by includeFilterSetViewModel.filters.collectAsState(emptyList())
                LazyColumn(
                    modifier = Modifier
                        .width(columnWidth)
                        .fillMaxHeight()
                        .padding(Dimens.mttPadding),
                    state = stateForLazyListOfIncludeFilters,
                    verticalArrangement = Arrangement.spacedBy(Dimens.mttPadding),
                )
                {
                    // toggle button for enabling/disabling word wrapping in the log viewer
                    item()
                    {
                        ToggleButton(
                            onClick = { viewModel.toggleWordWrap() },
                            isToggled = shouldWrapText,
                            isToggledColors = ButtonDefaults.buttonColors(themeColors.primary),
                            isNotToggledColors = ButtonDefaults.buttonColors(themeColors.surface),
                            content = { contentColor -> IconWrapText(contentColor) },
                        )
                    }

                    // reorderable list of working files
                    collapsableEditWorkingFilesSection(
                        themeColors = themeColors,
                        lazyColumnItemKeyPrefix = "showEditFileListPanel",
                        reorderableLazyListState = reorderableLazyListStateForLazyListOfIncludeFilters,
                        sectionIcon = { contentColor -> IconEditFileList(contentColor) },
                        itemViewModels = workingFileItemViewModels,
                        sectionTitle = "Edit file list",
                        shouldShowSectionHeader = isSidebarExpanded,
                        isSectionExpanded = showEditFileListPanel,
                        requestToggleSectionExpanded = { showEditFileListPanel = !showEditFileListPanel },
                        requestAddNewWorkingFile = { },
                    )

                    // exclude filters
                    collapsableEditFilterSection(
                        themeColors = themeColors,
                        lazyColumnItemKeyPrefix = "showExcludeFilterPanel",
                        reorderableLazyListState = reorderableLazyListStateForLazyListOfIncludeFilters,
                        sectionIcon = { contentColor -> IconExcludeFilter(contentColor) },
                        filterItemViewModels = excludeFilterItemViewModels,
                        sectionTitle = "Exclude filters",
                        shouldShowSectionHeader = isSidebarExpanded,
                        isSectionExpanded = showExcludeFilterPanel,
                        requestToggleSectionExpanded = { showExcludeFilterPanel = !showExcludeFilterPanel },
                        expandedFilterId = filterIdOfSelectedFilterEditorPanel,
                        requestFilterExpansion = { filterId -> filterIdOfSelectedFilterEditorPanel = filterId },
                        requestAddNewFilter = excludeFilterSetViewModel::addFilter,
                        filterType = FilterType.EXCLUDE,
                    )

                    // include filters
                    collapsableEditFilterSection(
                        themeColors = themeColors,
                        lazyColumnItemKeyPrefix = "showIncludeFilterPanel",
                        reorderableLazyListState = reorderableLazyListStateForLazyListOfIncludeFilters,
                        sectionIcon = { contentColor -> IconIncludeFilter(contentColor) },
                        filterItemViewModels = includeFilterItemViewModels,
                        sectionTitle = "Include filters",
                        shouldShowSectionHeader = isSidebarExpanded,
                        isSectionExpanded = showIncludeFilterPanel,
                        requestToggleSectionExpanded = { showIncludeFilterPanel = !showIncludeFilterPanel },
                        expandedFilterId = filterIdOfSelectedFilterEditorPanel,
                        requestFilterExpansion = { filterId -> filterIdOfSelectedFilterEditorPanel = filterId },
                        requestAddNewFilter = includeFilterSetViewModel::addFilter,
                        filterType = FilterType.INCLUDE,
                    )
                }
            }
        }

        // endregion
    }
}
