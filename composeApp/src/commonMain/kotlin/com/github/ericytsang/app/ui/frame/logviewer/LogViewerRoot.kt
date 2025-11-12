@file:OptIn(ExperimentalMaterialApi::class,ExperimentalComposeUiApi::class)

package com.github.ericytsang.app.ui.frame.logviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Colors
import androidx.compose.material.ContentAlpha
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.util.ChildWindowManager
import com.github.ericytsang.app.ui.util.asset.IconEditFileList
import com.github.ericytsang.app.ui.util.asset.IconExcludeFilter
import com.github.ericytsang.app.ui.util.asset.IconIncludeFilter
import com.github.ericytsang.app.ui.util.asset.IconWrapText
import com.github.ericytsang.app.ui.util.component.ColorCodedLogLine
import com.github.ericytsang.app.ui.util.component.CommonWindowHeader
import com.github.ericytsang.app.ui.util.component.LazyColumnWithScrollbar
import com.github.ericytsang.app.ui.util.component.ToggleButton
import com.github.ericytsang.app.ui.util.openMultiFilePicker
import com.github.ericytsang.app.ui.util.openNewProjectWizard
import com.github.ericytsang.app.ui.util.openProjectBrowser
import com.github.ericytsang.domain.objects.FilePath.Companion.toFile
import com.github.ericytsang.domain.objects.FilterId
import com.github.ericytsang.domain.objects.FilterType
import com.github.ericytsang.domain.objects.Theme
import com.github.ericytsang.domain.objects.WorkingFileSetEmpty
import kotlinx.coroutines.CoroutineScope
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * controls whether the UI should wrap text horizontally or scroll horizontally.
 */
sealed class HorizontalMode
{

    data object WordWrap:HorizontalMode()

    /**
     * [longestLineLength] is used to determine the width of the horizontal scrollbar.
     * all list items are also forced to have the same width, as the longest line.
     * because when the line width is "fillMaxWidth", since it is inside a parent that has a horizontal scrollbar,
     * then the max width is i suppose infinite, and then it will end up just making the width of the item only wrap the
     * content.
     */
    data class Scroll(
        /**
         * used to determine the width of the horizontal scrollbar - since using fixed-width font,
         * this is enough; no need to save the exact text of the longest line.
         */
        val longestLineLength:Int = 0,
    ):HorizontalMode()
}

@Composable
fun LogViewerRoot(
    window:ComposeWindow,
    themeColors:Colors,
    rootChildWindowManager:ChildWindowManager,
    logFileListViewModelFactory:()->LogFileListViewModel,
    filterSetViewModelFactory:(FilterType)->FilterSetViewModel,
    selectedItemsViewModelFactory:()->SelectedItemsViewModel<LogViewerViewModel.FileLineKey>,
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

    val selectedItemsViewModel = remember { selectedItemsViewModelFactory() }

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

            val logLinesState by logViewerViewModel.logLinesFlow.collectAsState(LogViewerViewModel.LogLinesState())
            val lazyListState = rememberLazyListState()
            val horizontalMode = if (shouldWrapText)
            {
                HorizontalMode.WordWrap
            }
            else
            {
                // calculate the longest line length for horizontal scrolling - we should "coerce at least" this with the width of the parent as well...
                val longestLineLength = logLinesState.logLines.maxOfOrNull { it.line.length } ?: 0
                HorizontalMode.Scroll(longestLineLength)
            }

            // Selection state
            val selectedItems by selectedItemsViewModel.selectedItemsFlow.collectAsState(IncludeSelected())
            var modifierState by remember { mutableStateOf(ModifierState()) }

            // track the width of the LazyColumnWithScrollbar, so we can set the min width of a log line to match this
            var lazyColumnWidth by remember { mutableStateOf(0) }

            // get managers
            val clipboardManager = LocalClipboard.current
            val focusManager = LocalFocusManager.current

            Surface(
                modifier = Modifier.weight(1f,fill = true),
                shape = MaterialTheme.shapes.small,
                border = ButtonDefaults.outlinedBorder,
            )
            {
                LazyColumnWithScrollbar(
                    enableHorizontalScroll = when (horizontalMode)
                    {
                        is HorizontalMode.WordWrap -> false
                        is HorizontalMode.Scroll -> true
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { size -> lazyColumnWidth = size.width }
                        .captureModifierState { newState -> modifierState = newState }
                        .handleKeyCombinations(
                            onSelectAll = { selectedItemsViewModel.selectAllItems(); true },
                            onDeselectAll = { selectedItemsViewModel.deselectAllItems(); true },
                            onCopySelection =
                            {
                                logViewerViewModel.copySelectedTextToClipboard(
                                    logLines = logLinesState.logLines,
                                    selectedItems = selectedItems,
                                    clipboardManager = clipboardManager,
                                )
                                true
                            },
                            onMoveFocusUp = { focusManager.moveFocus(FocusDirection.Up) },
                            onMoveFocusDown = { focusManager.moveFocus(FocusDirection.Down) },
                        )
                        .pointerInput("LogViewerRoot/logviewer/pointerInput")
                        {
                            detectDragGestures(
                                onDragStart = { offset ->

                                    // find the item at the mouse position
                                    val mousePosition = offset.y.toInt()
                                    val withIndex = lazyListState.layoutInfo.visibleItemsInfo
                                        .find { mousePosition in it.getOccupiedSpace() }
                                        ?.toDataClass()

                                    // get the item's key
                                    val key = withIndex?.key as LogViewerLazyListItemKey? ?: return@detectDragGestures
                                    val logLineItemKey = when (key)
                                    {
                                        is LogViewerLazyListItemKey.TopItem -> return@detectDragGestures
                                        is LogViewerLazyListItemKey.LogLineItem -> key
                                    }

                                    // start the dragging gesture
                                    selectedItemsViewModel.beginDragToSelect(
                                        keyOfItemAtDragStart = logLineItemKey.fileLineKey,
                                        selectionModifier = if (modifierState.isOsAgnosticCtrlPressed)
                                        {
                                            SelectedItemsViewModel.SelectionModifier.ADD_TO_SELECTION
                                        }
                                        else
                                        {
                                            SelectedItemsViewModel.SelectionModifier.REPLACE_SELECTION
                                        },
                                    )
                                },
                                onDrag = { change, dragAmount ->

                                    // find the item at the mouse position
                                    val mousePosition = change.position.y.toInt()
                                    val withIndex = lazyListState.layoutInfo.visibleItemsInfo
                                        .find { mousePosition in it.getOccupiedSpace() }
                                        ?.toDataClass()

                                    // get the item's key
                                    val key = withIndex?.key as LogViewerLazyListItemKey? ?: return@detectDragGestures
                                    val logLineItemKey = when (key)
                                    {
                                        is LogViewerLazyListItemKey.TopItem -> return@detectDragGestures
                                        is LogViewerLazyListItemKey.LogLineItem -> key
                                    }

                                    // update the dragging gesture selection state
                                    selectedItemsViewModel.updateDragToSelect(
                                        list = logLinesState.logLines,
                                        selector = { it.key },
                                        keyOfItemAtPointer = logLineItemKey.fileLineKey,
                                    )
                                },
                                onDragEnd = {
                                    selectedItemsViewModel.endDragToSelect()
                                },
                            )
                        },
                    lazyListState = lazyListState,
                )
                {
                    // put an item at top so that if new items are added to the top, the scroll will stick to the top.
                    // without this, when we re-order the top log line, the scroll would move to where the log line is
                    // being re-ordered to.
                    item(
                        key = LogViewerLazyListItemKey.TopItem(),
                        content = { Spacer(Modifier.size(1.dp)) }
                    )

                    // show log lines
                    items(
                        key = { index -> LogViewerLazyListItemKey.LogLineItem(logLinesState.logLines[index].key) },
                        count = logLinesState.logLines.size,
                    )
                    { index ->
                        val item = logLinesState.logLines[index]
                        val isSelected = item.key in selectedItems
                        val backgroundColor = if (isSelected) themeColors.primary.copy(alpha = ContentAlpha.medium) else themeColors.surface
                        val focusRequester = remember { FocusRequester() }
                        ColorCodedLogLine(
                            text = item.line,
                            invisibleText = when (horizontalMode)
                            {
                                is HorizontalMode.WordWrap -> ""
                                is HorizontalMode.Scroll -> (1..horizontalMode.longestLineLength).joinToString("") { " " }
                            },
                            modifier = Modifier
                                .animateItem() // i want to enable these animations, but uh, it causes some portion of the log line to start flashing, and I think it is a bug in compose, so I am disabling them for now.
                                .fillParentMaxWidth()
                                .widthIn(min = lazyColumnWidth.dp)
                                .background(backgroundColor)
                                .focusRequester(focusRequester)
                                .clickable()
                                {
                                    when
                                    {
                                        // shift+click: select range, keep others
                                        modifierState.isShiftPressed -> selectedItemsViewModel.selectRange(
                                            list = logLinesState.logLines,
                                            selector = { it.key },
                                            keyOfItemAtEndOfRange = item.key,
                                            selectionModifier = if (modifierState.isOsAgnosticCtrlPressed)
                                            {
                                                SelectedItemsViewModel.SelectionModifier.ADD_TO_SELECTION
                                            }
                                            else
                                            {
                                                SelectedItemsViewModel.SelectionModifier.REPLACE_SELECTION
                                            },
                                        )

                                        // ctrl/cmd+click: toggle
                                        modifierState.isOsAgnosticCtrlPressed ->
                                            selectedItemsViewModel.toggleItemSelection(item.key)

                                        // normal click: select only this
                                        else -> selectedItemsViewModel.selectItem(item.key)
                                    }
                                    focusRequester.requestFocus()
                                }
                                .padding(horizontal = Dimens.mttPadding),
                            delimiters = delimiters,
                            themeForColorCoding = theme,
                            defaultColor = themeColors.onBackground,
                            softWrap = shouldWrapText,
                        )
                    }
                }
            }

            // endregion

            // region filter helpers and working file set editor

            var expandedPanels by remember { mutableStateOf(setOf<String>()) }

            var filterIdOfSelectedFilterEditorPanel by remember { mutableStateOf<FilterId?>(null) }

            val isSidebarExpanded by derivedStateOf { expandedPanels.isNotEmpty() }

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
                    val showEditFileListPanel = "showEditFileListPanel"
                    collapsableEditWorkingFilesSection(
                        themeColors = themeColors,
                        lazyColumnItemKeyPrefix = showEditFileListPanel,
                        reorderableLazyListState = reorderableLazyListStateForLazyListOfIncludeFilters,
                        sectionIcon = { contentColor -> IconEditFileList(contentColor) },
                        itemViewModels = workingFileItemViewModels,
                        sectionTitle = "Edit file list",
                        shouldShowSectionHeader = isSidebarExpanded,
                        isSectionExpanded = showEditFileListPanel in expandedPanels,
                        requestToggleSectionExpanded = { expandedPanels = toggle(showEditFileListPanel,expandedPanels) },
                        requestAddNewWorkingFile =
                        {
                            openMultiFilePicker(window)
                            { selectedFiles ->
                                workingFileSetEditorViewModel.addFiles(selectedFiles)
                            }
                        },
                    )

                    // exclude filters
                    val showExcludeFilterPanel = "showExcludeFilterPanel"
                    collapsableEditFilterSection(
                        themeColors = themeColors,
                        lazyColumnItemKeyPrefix = showExcludeFilterPanel,
                        reorderableLazyListState = reorderableLazyListStateForLazyListOfIncludeFilters,
                        sectionIcon = { contentColor -> IconExcludeFilter(contentColor) },
                        filterItemViewModels = excludeFilterItemViewModels,
                        sectionTitle = "Exclude filters",
                        shouldShowSectionHeader = isSidebarExpanded,
                        isSectionExpanded = showExcludeFilterPanel in expandedPanels,
                        requestToggleSectionExpanded = { expandedPanels = toggle(showExcludeFilterPanel,expandedPanels) },
                        expandedFilterId = filterIdOfSelectedFilterEditorPanel,
                        requestFilterExpansion = { filterId -> filterIdOfSelectedFilterEditorPanel = filterId },
                        requestAddNewFilter = excludeFilterSetViewModel::addFilter,
                        filterType = FilterType.EXCLUDE,
                    )

                    // include filters
                    val showIncludeFilterPanel = "showIncludeFilterPanel"
                    collapsableEditFilterSection(
                        themeColors = themeColors,
                        lazyColumnItemKeyPrefix = showIncludeFilterPanel,
                        reorderableLazyListState = reorderableLazyListStateForLazyListOfIncludeFilters,
                        sectionIcon = { contentColor -> IconIncludeFilter(contentColor) },
                        filterItemViewModels = includeFilterItemViewModels,
                        sectionTitle = "Include filters",
                        shouldShowSectionHeader = isSidebarExpanded,
                        isSectionExpanded = showIncludeFilterPanel in expandedPanels,
                        requestToggleSectionExpanded = { expandedPanels = toggle(showIncludeFilterPanel,expandedPanels) },
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

private fun toggle(
    item:String,
    set:Set<String>,
):Set<String> = if (item in set)
{
    set - item
}
else
{
    set + item
}

fun LazyListItemInfo.getOccupiedSpace():IntRange
{
    return offset..(offset+size)
}

fun LazyListItemInfo.toDataClass() = LazyListItemInfoDataClass(
    index = index,
    offset = offset,
    size = size,
    key = key,
)

data class LazyListItemInfoDataClass(
    /** [LazyListItemInfo.index] */
    val index:Int,
    /** [LazyListItemInfo.offset] */
    val offset:Int,
    /** [LazyListItemInfo.size] */
    val size:Int,
    /** [LazyListItemInfo.key] */
    val key:Any,
)

sealed interface LogViewerLazyListItemKey
{
    data class TopItem(
        val unit:Unit = Unit,
    ):LogViewerLazyListItemKey

    data class LogLineItem(
        val fileLineKey:LogViewerViewModel.FileLineKey,
    ):LogViewerLazyListItemKey
}
