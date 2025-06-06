@file:OptIn(ExperimentalMaterialApi::class)

package com.github.ericytsang.app.ui.frame.logviewer

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.Colors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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

@Composable
fun LogViewerRoot(
    window:ComposeWindow,
    themeColors:Colors,
    rootChildWindowManager:ChildWindowManager,
    workingFileSetEditorViewModelFactory:()->WorkingFileSetEditorViewModel,
    viewModelFactory:(CoroutineScope)->LogViewerRootViewModel = { uiScope -> LogViewerRootViewModel.create(uiScope) },
    logViewerViewModelFactory:()->LogViewerViewModel = { LogViewerViewModel.createDefault() },
)
{
    val uiScope = rememberCoroutineScope()
    val viewModel = remember { viewModelFactory(uiScope) }
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

            val placeholderFilters = listOf(
                FilterViewModelImpl(
                    filterId = FilterId("ID1"),
                    initialFilterString = "filterStringFlow",
                    initialIsCaseSensitive = true,
                    initialIsEnabled = true,
                    initialFilterType = FilterType.STRING_LITERAL,
                    onRequestDelete = { },
                ),
                FilterViewModelImpl(
                    filterId = FilterId("ID2"),
                    initialFilterString = "filterStringFlow",
                    initialIsCaseSensitive = true,
                    initialIsEnabled = true,
                    initialFilterType = FilterType.STRING_LITERAL,
                    onRequestDelete = { },
                ),
                FilterViewModelImpl(
                    filterId = FilterId("ID3"),
                    initialFilterString = "filterStringFlow",
                    initialIsCaseSensitive = true,
                    initialIsEnabled = true,
                    initialFilterType = FilterType.STRING_LITERAL,
                    onRequestDelete = { },
                ),
            )

            var expandedItem by remember { mutableStateOf(FilterId("nothing should be selected right now")) }

            val columnWidth by derivedStateOf {
                if (showEditFileListPanel || showExcludeFilterPanel || showIncludeFilterPanel)
                    Modifier.width(400.dp)
                else
                    Modifier.width(Dimens.mttPadding*2+Dimens.mttSize)
            }

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
                    item(key = "showEditFileListPanelHeader") {
                        Row(modifier = Modifier.animateItem()) {
                            ToggleButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { showEditFileListPanel = !showEditFileListPanel },
                                isToggled = showEditFileListPanel,
                                isToggledColors = ButtonDefaults.buttonColors(themeColors.primary),
                                isNotToggledColors = ButtonDefaults.buttonColors(themeColors.surface),
                                content = { contentColor -> IconEditFileList(contentColor) },
                            )
                        }
                    }

                    if (showEditFileListPanel)
                    {
                        item(key = "editFileListPanel") {
                            Row(modifier = Modifier.animateItem()) {
                                FilterBuilderPanel(
                                    themeColors = themeColors,
                                    filterViewModels = placeholderFilters,
                                    expandedItem = expandedItem,
                                    onTextFieldGotFocus = { filterId -> expandedItem = filterId },
                                )
                            }
                        }
                    }

                    item(key = "showExcludeFilterPanelHeader") {
                        Row(modifier = Modifier.animateItem()) {
                            ToggleButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { showExcludeFilterPanel = !showExcludeFilterPanel },
                                isToggled = showExcludeFilterPanel,
                                isToggledColors = ButtonDefaults.buttonColors(themeColors.primary),
                                isNotToggledColors = ButtonDefaults.buttonColors(themeColors.surface),
                                content = { contentColor -> IconExcludeFilter(contentColor) },
                            )
                        }
                    }

                    if (showExcludeFilterPanel)
                    {
                        item(key = "excludeFilterPanel") {
                            Row(modifier = Modifier.animateItem()) {
                                FilterBuilderPanel(
                                    themeColors = themeColors,
                                    filterViewModels = placeholderFilters,
                                    expandedItem = expandedItem,
                                    onTextFieldGotFocus = { filterId -> expandedItem = filterId },
                                )
                            }
                        }
                    }

                    item(key = "showIncludeFilterPanelHeader") {
                        Row(modifier = Modifier.animateItem()) {
                            ToggleButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { showIncludeFilterPanel = !showIncludeFilterPanel },
                                isToggled = showIncludeFilterPanel,
                                isToggledColors = ButtonDefaults.buttonColors(themeColors.primary),
                                isNotToggledColors = ButtonDefaults.buttonColors(themeColors.surface),
                                content = { contentColor -> IconIncludeFilter(contentColor) },
                            )
                        }
                    }

                    if (showIncludeFilterPanel)
                    {
                        item(key = "includeFilterPanel") {
                            Row(modifier = Modifier.animateItem()) {
                                FilterBuilderPanel(
                                    themeColors = themeColors,
                                    filterViewModels = placeholderFilters,
                                    expandedItem = expandedItem,
                                    onTextFieldGotFocus = { filterId -> expandedItem = filterId },
                                )
                            }
                        }
                    }
                }
            }

            // endregion
        }
    }
}

/**
 * a region on the UI allowing user to add/remove filters to some collection of filters.
 * filters have different interpretation types: string literal, regex, logcat filter.
 * filters can be case-sensitive or not.
 * filters can be enabled or disabled.
 * each filter is represented on the UI by a text field and a checkbox.
 * when the user clicks on the checkbox, the filter is enabled or disabled.
 * when the user types in the text field, the filter is updated.
 * when the text field has focus, the other settings are revealed below the text field:
 * - case sensitivity toggle
 * - filter type toggle.
 */
@Composable
fun FilterBuilderPanel(

    /**
     * colors to be used for the UI components.
     * this is used to ensure that the UI components are consistent with the theme.
     */
    themeColors:Colors,

    /**
     * flow that emits the list of filters to be displayed.
     * each filter is represented by a FilterViewModel.
     */
    filterViewModels:List<FilterViewModel>,

    /**
     * flow that emits the id of the filter that is currently expanded.
     * this is used to determine whether to show the additional settings for a filter.
     */
    expandedItem:FilterId,

    /**
     * callback when the text field gets focus.
     * this is intended to be used by the host to update [expandedItem].
     */
    onTextFieldGotFocus:(FilterId)->Unit,

    //mutableHasSelectableItems:MutableHasSelectableItems<FilterId>
)
{
    Column(
        modifier = Modifier.padding(Dimens.mttPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.mttPadding),
        horizontalAlignment = Alignment.Start,
    )
    {
        for (filterViewModel in filterViewModels)
        {
            Column {
                val textFieldInteractionSource = remember { MutableInteractionSource() }

                // call onTextFieldGotFocus one time when the text field gets focus
                val isTextFieldFocused by textFieldInteractionSource.collectIsFocusedAsState()
                var makeSureOnlyCalledOneTime by remember { mutableStateOf(false) }
                if (isTextFieldFocused && !makeSureOnlyCalledOneTime)
                {
                    makeSureOnlyCalledOneTime = true
                    onTextFieldGotFocus(filterViewModel.filterId)
                }
                else if (!isTextFieldFocused)
                {
                    makeSureOnlyCalledOneTime = false
                }

                // show editable filter string and checkbox for enabling/disabling the filter
                Row(verticalAlignment = Alignment.CenterVertically)
                {
                    Checkbox(
                        checked = filterViewModel.isEnabledFlow.collectAsState(false).value,
                        onCheckedChange = { newValue -> filterViewModel.setEnabled(newValue) },
                    )
                    TextField(
                        value = filterViewModel.filterStringFlow.collectAsState("").value,
                        interactionSource = textFieldInteractionSource,
                        onValueChange = { newValue -> filterViewModel.setFilterString(newValue) },
                        modifier = Modifier.weight(1f, fill = true).padding(end = Dimens.mttPadding),
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = themeColors.onBackground,
                        ),
                    )
                }

                // show the additional settings for this filter if it is expanded ([expandedItem])
                if (filterViewModel.filterId == expandedItem)
                {
                    // checkbox for case sensitivity
                    val isCaseSensitive by filterViewModel.isCaseSensitiveFlow.collectAsState(false)
                    val toggleCaseSensitivity = { filterViewModel.setCaseSensitive(!isCaseSensitive) }
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(start = Dimens.mttSize),
                        shape = MaterialTheme.shapes.small,
                        onClick = toggleCaseSensitivity,
                    )
                    {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        )
                        {
                            Checkbox(
                                checked = isCaseSensitive,
                                onCheckedChange = { newValue -> toggleCaseSensitivity() },
                            )
                            Text("Case sensitive")
                        }
                    }

                    // spacing
                    Spacer(modifier = Modifier.size(Dimens.mttPadding))

                    // radio buttons for filter type
                    Text("Filter type", modifier = Modifier.padding(start = Dimens.mttSize))
                    val filterTypeFlow by filterViewModel.filterTypeFlow.collectAsState(null)
                    for (filterType in FilterType.entries)
                    {
                        val onClick = { filterViewModel.setFilterType(filterType) }
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(start = Dimens.mttSize),
                            shape = MaterialTheme.shapes.small,
                            onClick = onClick,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = filterTypeFlow == filterType,
                                    onClick = onClick,
                                )
                                Text(filterType.displayName)
                            }
                        }
                    }
                }
            }
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
    private val onRequestDelete:()->Unit,
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
        onRequestDelete()
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
