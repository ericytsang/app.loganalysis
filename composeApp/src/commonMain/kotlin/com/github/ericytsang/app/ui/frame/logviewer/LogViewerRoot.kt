@file:OptIn(ExperimentalMaterialApi::class)

package com.github.ericytsang.app.ui.frame.logviewer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Colors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
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
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.frame.workingfileseteditor.WorkingFileSetEditorViewModel
import com.github.ericytsang.app.ui.util.ChildWindowManager
import com.github.ericytsang.app.ui.util.asset.IconEditFileList
import com.github.ericytsang.app.ui.util.asset.IconExcludeFilter
import com.github.ericytsang.app.ui.util.asset.IconIncludeFilter
import com.github.ericytsang.app.ui.util.asset.IconMatchCase
import com.github.ericytsang.app.ui.util.asset.IconWrapText
import com.github.ericytsang.app.ui.util.component.ColorCodedLogLine
import com.github.ericytsang.app.ui.util.component.CommonWindowHeader
import com.github.ericytsang.app.ui.util.component.ToggleButton
import com.github.ericytsang.app.ui.util.openNewProjectWizard
import com.github.ericytsang.app.ui.util.openProjectBrowser
import com.github.ericytsang.domain.objects.Theme
import com.github.ericytsang.domain.objects.WorkingFileSetEmpty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.UUID

interface FilterSetViewModel
{
    val filters:Flow<List<FilterViewModel>>
    fun addFilter(filter:FilterViewModel)
}

class FilterSetViewModelImpl:FilterSetViewModel
{
    private val _filters = MutableStateFlow<List<FilterViewModel>>(createPlaceholders())
    override val filters:Flow<List<FilterViewModel>> get() = _filters

    override fun addFilter(filter:FilterViewModel)
    {
        _filters.update { it + filter }
    }

    private fun removeFilter(filterId:FilterId)
    {
        _filters.update { it.filter { filter -> filter.filterId != filterId } }
    }

    private fun createPlaceholders() = listOf(
        FilterViewModelImpl(
            filterId = FilterId(UUID.randomUUID().toString()),
            initialFilterString = "filterStringFlow",
            initialIsCaseSensitive = true,
            initialIsEnabled = true,
            initialFilterType = FilterType.STRING_LITERAL,
            onRequestDelete = ::removeFilter,
        ),
        FilterViewModelImpl(
            filterId = FilterId(UUID.randomUUID().toString()),
            initialFilterString = "filterStringFlow",
            initialIsCaseSensitive = true,
            initialIsEnabled = true,
            initialFilterType = FilterType.STRING_LITERAL,
            onRequestDelete = ::removeFilter,
        ),
        FilterViewModelImpl(
            filterId = FilterId(UUID.randomUUID().toString()),
            initialFilterString = "filterStringFlow",
            initialIsCaseSensitive = true,
            initialIsEnabled = true,
            initialFilterType = FilterType.STRING_LITERAL,
            onRequestDelete = ::removeFilter,
        ),
    )
}

@Composable
fun LogViewerRoot(
    window:ComposeWindow,
    themeColors:Colors,
    rootChildWindowManager:ChildWindowManager,
    includeFilterSetViewModelFactory:()->FilterSetViewModel = { FilterSetViewModelImpl() },
    excludeFilterSetViewModelFactory:()->FilterSetViewModel = { FilterSetViewModelImpl() },
    workingFileSetEditorViewModelFactory:()->WorkingFileSetEditorViewModel,
    viewModelFactory:(CoroutineScope)->LogViewerRootViewModel = { uiScope -> LogViewerRootViewModel.create(uiScope) },
    logViewerViewModelFactory:()->LogViewerViewModel = { LogViewerViewModel.createDefault() },
)
{
    val uiScope = rememberCoroutineScope()
    val viewModel = remember { viewModelFactory(uiScope) }
    val includeFilterSetViewModel = remember { includeFilterSetViewModelFactory() }
    val excludeFilterSetViewModel = remember { excludeFilterSetViewModelFactory() }
    val logViewerViewModel = remember { logViewerViewModelFactory() }
    val workingFileSetEditorViewModel = remember { workingFileSetEditorViewModelFactory() }

    val theme by viewModel.theme.collectAsState(Theme.DARK)

    val logcatFilterString by logViewerViewModel.logcatFilterString.collectAsState("")
    val delimiters by viewModel.getDelimiterFlow().collectAsState("")
    val shouldMatchCase by logViewerViewModel.isCaseSensitive.collectAsState(false)

    val workingFileSet by workingFileSetEditorViewModel.workingFileSet.collectAsState(WorkingFileSetEmpty)
    logViewerViewModel.setConcatenatedFiles(workingFileSet.files.map { File(it.filePath) })

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

            // new project button
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
            // logcat filter input and log viewer
            Column(
                modifier = Modifier.weight(1f,fill = true),
                verticalArrangement = Arrangement.spacedBy(Dimens.mttPadding),
            )
            {

                // logcat filter input
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    border = ButtonDefaults.outlinedBorder,
                )
                {
                    Row(
                        modifier = Modifier.padding(Dimens.mttPadding),
                        verticalAlignment = Alignment.CenterVertically,
                    )
                    {

                        // user input for entering the logcat filter string
                        TextField(
                            value = logcatFilterString,
                            onValueChange = { newValue -> logViewerViewModel.setLogcatFilterString(newValue) },
                            modifier = Modifier.weight(1f).padding(end = Dimens.mttPadding),
                            colors = TextFieldDefaults.textFieldColors(
                                textColor = themeColors.onBackground,
                            ),
                        )

                        // toggle button for enabling/disabling case-sensitive matching in the logcat filter
                        ToggleButton(
                            modifier = Modifier.padding(end = Dimens.mttPadding),
                            onClick = { logViewerViewModel.setCaseSensitive(!logViewerViewModel.isCaseSensitive.value) },
                            isToggled = shouldMatchCase,
                            isToggledColors = ButtonDefaults.buttonColors(themeColors.primary),
                            isNotToggledColors = ButtonDefaults.buttonColors(themeColors.surface),
                            content = { contentColor -> IconMatchCase(contentColor) },
                        )

                        // toggle button for enabling/disabling word wrapping in the log viewer
                        ToggleButton(
                            modifier = Modifier.padding(end = Dimens.mttPadding),
                            onClick = { viewModel.toggleWordWrap() },
                            isToggled = shouldWrapText,
                            isToggledColors = ButtonDefaults.buttonColors(themeColors.primary),
                            isNotToggledColors = ButtonDefaults.buttonColors(themeColors.surface),
                            content = { contentColor -> IconWrapText(contentColor) },
                        )
                    }
                }

                // region log viewer

                val logLines by logViewerViewModel.getLogLinesFlow().collectAsState(emptyList())

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small,
                    border = ButtonDefaults.outlinedBorder,
                )
                {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    )
                    {
                        items(count = logLines.size)
                        { index ->
                            ColorCodedLogLine(
                                text = logLines[index],
                                modifier = Modifier.fillMaxWidth(),
                                delimiters = delimiters,
                                themeForColorCoding = theme,
                                defaultColor = themeColors.onBackground,
                                softWrap = shouldWrapText,
                            )
                        }
                    }
                }

                // endregion
            }

            // region filter helpers and working file set editor

            var showEditFileListPanel by remember { mutableStateOf(false) }
            var showExcludeFilterPanel by remember { mutableStateOf(false) }
            var showIncludeFilterPanel by remember { mutableStateOf(false) }

            var filterIdOfSelectedFilterEditorPanel by remember { mutableStateOf(FilterId("nothing should be selected right now")) }

            val columnWidth by derivedStateOf {
                if (showEditFileListPanel || showExcludeFilterPanel || showIncludeFilterPanel)
                    Modifier.width(400.dp)
                else
                    Modifier.width(Dimens.mttPadding*2+Dimens.mttSize)
            }

            val excludeFilterSet by excludeFilterSetViewModel.filters.collectAsState(emptyList())
            val includeFilterSet by includeFilterSetViewModel.filters.collectAsState(emptyList())

            Surface(
                modifier = Modifier.fillMaxHeight(),
                shape = MaterialTheme.shapes.small,
                border = ButtonDefaults.outlinedBorder,
            )
            {
                LazyColumn(
                    modifier = columnWidth
                        .fillMaxHeight()
                        .padding(Dimens.mttPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimens.mttPadding),
                )
                {
                    collapsableEditFilterSection(
                        themeColors = themeColors,
                        lazyColumnItemKeyPrefix = "showEditFileListPanel",
                        sectionIcon = { contentColor -> IconEditFileList(contentColor) },
                        isSectionExpanded = showEditFileListPanel,
                        requestToggleSectionExpanded = { showEditFileListPanel = !showEditFileListPanel },
                        placeholderFilters = excludeFilterSet,
                        expandedFilterId = filterIdOfSelectedFilterEditorPanel,
                        requestFilterExpansion = { filterId -> filterIdOfSelectedFilterEditorPanel = filterId },
                    )

                    collapsableEditFilterSection(
                        themeColors = themeColors,
                        lazyColumnItemKeyPrefix = "showExcludeFilterPanel",
                        sectionIcon = { contentColor -> IconExcludeFilter(contentColor) },
                        isSectionExpanded = showExcludeFilterPanel,
                        requestToggleSectionExpanded = { showExcludeFilterPanel = !showExcludeFilterPanel },
                        placeholderFilters = excludeFilterSet,
                        expandedFilterId = filterIdOfSelectedFilterEditorPanel,
                        requestFilterExpansion = { filterId -> filterIdOfSelectedFilterEditorPanel = filterId },
                    )

                    collapsableEditFilterSection(
                        themeColors = themeColors,
                        lazyColumnItemKeyPrefix = "showIncludeFilterPanel",
                        sectionIcon = { contentColor -> IconIncludeFilter(contentColor) },
                        isSectionExpanded = showIncludeFilterPanel,
                        requestToggleSectionExpanded = { showIncludeFilterPanel = !showIncludeFilterPanel },
                        placeholderFilters = includeFilterSet,
                        expandedFilterId = filterIdOfSelectedFilterEditorPanel,
                        requestFilterExpansion = { filterId -> filterIdOfSelectedFilterEditorPanel = filterId },
                    )
                }
            }

            // endregion
        }
    }
}

sealed class SelectedItems<T>
{
    fun isSelected(subject:T):Boolean = when (this)
    {
        is SelectedSome -> subject in selectedItems
        is DeselectedSome -> subject !in deselectedItems
    }

    data class SelectedSome<T>(val selectedItems:Set<T>):SelectedItems<T>()
    data class DeselectedSome<T>(val deselectedItems:Set<T>):SelectedItems<T>()
}

interface MutableHasSelectableItems<T>:HasSelectableItems<T>
{
    fun addToSelection(item:T)
    fun removeFromSelection(item:T)
    fun deselectAll()
    fun selectAll()
}

interface HasSelectableItems<T>
{
    val selectedItems:Flow<SelectedItems<T>>
}

class HasSelectableItemsImpl<T>(
    initialSelection:SelectedItems<T> = SelectedItems.SelectedSome(emptySet()),
):MutableHasSelectableItems<T>
{
    private val _selectedItems = MutableStateFlow<SelectedItems<T>>(initialSelection)
    override val selectedItems:Flow<SelectedItems<T>> get() = _selectedItems
    override fun addToSelection(item:T)
    {
        _selectedItems.update { old ->
            when (old)
            {
                is SelectedItems.SelectedSome -> SelectedItems.SelectedSome(old.selectedItems+item)
                is SelectedItems.DeselectedSome -> SelectedItems.DeselectedSome(old.deselectedItems-item)
            }
        }
    }

    override fun removeFromSelection(item:T)
    {
        _selectedItems.update { old ->
            when (old)
            {
                is SelectedItems.SelectedSome -> SelectedItems.SelectedSome(old.selectedItems-item)
                is SelectedItems.DeselectedSome -> SelectedItems.DeselectedSome(old.deselectedItems+item)
            }
        }
    }

    override fun deselectAll()
    {
        _selectedItems.value = SelectedItems.SelectedSome(emptySet())
    }

    override fun selectAll()
    {
        _selectedItems.value = SelectedItems.DeselectedSome(emptySet())
    }
}

class FilterViewModelImpl(
    override val filterId:FilterId,
    initialFilterString:String,
    initialIsCaseSensitive:Boolean,
    initialIsEnabled:Boolean,
    initialFilterType:FilterType,
    private val onRequestDelete:(FilterId)->Unit,
):FilterViewModel
{

    private val _filterStringFlow = MutableStateFlow<String>(initialFilterString)
    override val filterStringFlow:Flow<String> get() = _filterStringFlow

    private val _isCaseSensitiveFlow = MutableStateFlow<Boolean>(initialIsCaseSensitive)
    override val isCaseSensitiveFlow:Flow<Boolean> get() = _isCaseSensitiveFlow

    private val _isEnabledFlow = MutableStateFlow<Boolean>(initialIsEnabled)
    override val isEnabledFlow:Flow<Boolean> get() = _isEnabledFlow

    private val _filterTypeFlow = MutableStateFlow<FilterType>(initialFilterType)
    override val filterTypeFlow:Flow<FilterType> get() = _filterTypeFlow

    override fun setFilterString(newValue:String)
    {
        _filterStringFlow.value = newValue
    }

    override fun setCaseSensitive(newValue:Boolean)
    {
        _isCaseSensitiveFlow.value = newValue
    }

    override fun setEnabled(newValue:Boolean)
    {
        _isEnabledFlow.value = newValue
    }

    override fun setFilterType(newValue:FilterType)
    {
        _filterTypeFlow.value = newValue
    }

    override fun requestDelete()
    {
        onRequestDelete(filterId)
    }
}

data class FilterId(val id:String)

interface FilterViewModel
{
    val filterId:FilterId

    val filterStringFlow:Flow<String>
    val isCaseSensitiveFlow:Flow<Boolean>
    val isEnabledFlow:Flow<Boolean>
    val filterTypeFlow:Flow<FilterType>

    fun setFilterString(newValue:String)
    fun setCaseSensitive(newValue:Boolean)
    fun setEnabled(newValue:Boolean)
    fun setFilterType(newValue:FilterType)
    fun requestDelete()
}

enum class FilterType(
    val displayName:String,
)
{
    STRING_LITERAL("Plaintext"),
    REGEX("Regex"),
    LOGCAT_FILTER("Logcat filter"),
}
