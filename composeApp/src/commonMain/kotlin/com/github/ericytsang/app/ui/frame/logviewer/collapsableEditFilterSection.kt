package com.github.ericytsang.app.ui.frame.logviewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.Colors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.util.asset.IconCollapseSection
import com.github.ericytsang.app.ui.util.asset.IconDragHandle
import com.github.ericytsang.app.ui.util.component.DoubleClickButton
import com.github.ericytsang.domain.objects.FilterId
import com.github.ericytsang.domain.objects.FilterInterpretationMode
import com.github.ericytsang.domain.objects.FilterType
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState

@ExperimentalMaterialApi
fun LazyListScope.collapsableEditFilterSection(

    /** colors to be used for the UI components. */
    themeColors:Colors,

    /**
     * [LazyColumn] uses keys to keep track of its contained items
     * so it can infer what animations to perform as items CRUD
     */
    lazyColumnItemKeyPrefix:String,

    /**
     * [ReorderableLazyListState] to be used when creating reorderable items.
     * this is used to allow drag-and-drop reordering of the filters.
     */
    reorderableLazyListState: ReorderableLazyListState,

    /**
     * composable to be used as the icon for the section header.
     * this is intended to be used by the host to provide a custom icon for the section header.
     */
    sectionIcon:@Composable (contentColor:Color)->Unit,

    /**
     * what to show as the title of the section when the section is expanded.
     */
    sectionTitle:String,

    /**
     * whether this section is expanded or not.
     * if it is expanded, the filter builder panel is shown.
     */
    isSectionExpanded:Boolean,

    /**
     * whether to show the section header.
     * this is intended to be used by the host to hide the section header when it is not needed.
     * currently the intention is to show the section header while the sidebar is expanded.
     */
    shouldShowSectionHeader:Boolean,

    /**
     * callback to request toggling the section expanded state.
     * this is intended to be used by the host to update [isSectionExpanded].
     */
    requestToggleSectionExpanded:()->Unit,

    /**
     * list of filters to be displayed in the filter builder panel.
     * each filter is represented by a [FilterViewModel].
     */
    filterItemViewModels:List<FilterViewModel>,

    /**
     * the id of the filter that is currently expanded.
     * this is used to determine whether to show the additional settings for a filter.
     */
    expandedFilterId:FilterId?,

    /**
     * callback when the text field gets focus.
     * this is intended to be used by the host to update [expandedFilterId].
     */
    requestFilterExpansion:(FilterId?)->Unit,

    /**
     * callback to request adding a new filter.
     * this is intended to be used by the host to add a new filter.
     */
    requestAddNewFilter:()->Unit,

    /**
     * type of the filter that is being edited.
     */
    filterType:FilterType,
)
{

    // header for the section
    collapsableSectionHeader(
        themeColors = themeColors,
        lazyColumnItemKeyPrefix = lazyColumnItemKeyPrefix,
        sectionIcon = sectionIcon,
        sectionTitle = sectionTitle,
        isSectionExpanded = isSectionExpanded,
        shouldShowSectionHeader = shouldShowSectionHeader,
        requestToggleSectionExpanded = requestToggleSectionExpanded,
    )

    // if the section is expanded, show the filter builder panel
    if (isSectionExpanded)
    {
        // button to add a new filter
        val itemKey = ReorderableSidebarItemKey.NewFilterButton(filterType)
        item(key = itemKey)
        {
            ReorderableItem(
                state = reorderableLazyListState,
                key = itemKey,
                enabled = filterItemViewModels.isEmpty(),
            )
            {
                Row(modifier = Modifier.animateItem().background(themeColors.background))
                {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth().animateItem().padding(Dimens.mttPadding),
                        onClick = requestAddNewFilter,
                        colors = ButtonDefaults.outlinedButtonColors(
                            backgroundColor = themeColors.surface,
                            contentColor = themeColors.onSurface,
                        ),
                    )
                    {
                        Text("Add new filter")
                    }
                }
            }
        }

        // show the filters that the user can edit
        filterBuilderPanel(
            themeColors = themeColors,
            reorderableLazyListState = reorderableLazyListState,
            filterViewModels = filterItemViewModels,
            expandedItem = expandedFilterId,
            onChangeExpandedItem = requestFilterExpansion,
        )
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
@OptIn(ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
fun LazyListScope.filterBuilderPanel(

    /**
     * colors to be used for the UI components.
     * this is used to ensure that the UI components are consistent with the theme.
     */
    themeColors:Colors,

    /**
     * [ReorderableLazyListState] to be used when creating reorderable items.
     * this is used to allow drag-and-drop reordering of the filters.
     */
    reorderableLazyListState: ReorderableLazyListState,

    /**
     * flow that emits the list of filters to be displayed.
     * each filter is represented by a FilterViewModel.
     */
    filterViewModels:List<FilterViewModel>,

    /**
     * flow that emits the id of the filter that is currently expanded.
     * this is used to determine whether to show the additional settings for a filter.
     */
    expandedItem:FilterId?,

    /**
     * callback when the text field gets focus.
     * this is intended to be used by the host to update [expandedItem].
     */
    onChangeExpandedItem:(FilterId?)->Unit,
)
{
    for (filterViewModel in filterViewModels)
    {
        // checkbox for enable/disable the filter + text field for filter string
        val key = ReorderableSidebarItemKey.FilterItem(filterViewModel.filterId)
        item(key = key)
        {
            ReorderableItem(
                state = reorderableLazyListState,
                key = key,
            )
            { isDragging ->

                // increase elevation during dragging
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                Surface(elevation = elevation)
                {
                    Column(Modifier.fillMaxWidth().background(themeColors.background))
                    {

                        // show editable filter string and checkbox for enabling/disabling the filter
                        filterHeader(
                            themeColors = themeColors,
                            filterViewModel = filterViewModel,
                            onTextFieldGotFocus = onChangeExpandedItem,
                        )

                        // show the additional settings for this filter if it is expanded ([expandedItem])
                        AnimatedVisibility(filterViewModel.filterId == expandedItem)
                        {
                            filterProperties(
                                filterViewModel = filterViewModel,
                                themeColors = themeColors,
                                onCollapseSection = { onChangeExpandedItem(null) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Row
 * 1. drag-and-drop handle
 * 2. checkbox for enabling/disabling the filter
 * 3. editable filter string
 */
@Composable
private fun ReorderableCollectionItemScope.filterHeader(
    themeColors:Colors,
    filterViewModel:FilterViewModel,
    onTextFieldGotFocus:(FilterId)->Unit,
)
{
    Row(verticalAlignment = Alignment.CenterVertically)
    {

        // drag-and-drop handle
        IconButton(
            modifier = Modifier.draggableHandle(),
            onClick = {},
        )
        {
            IconDragHandle(themeColors.onSurface)
        }

        // checkbox for enabling/disabling the filter
        Checkbox(
            checked = filterViewModel.isEnabledFlow.collectAsState(false).value,
            onCheckedChange = { newValue -> filterViewModel.setEnabled(newValue) },
        )

        // interaction source for the text field
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

        // text field for filter string
        TextField(
            value = filterViewModel.filterStringFlow.collectAsState("").value,
            interactionSource = textFieldInteractionSource,
            onValueChange = { newValue -> filterViewModel.setFilterString(newValue) },
            modifier = Modifier.weight(1f,fill = true).padding(end = Dimens.mttPadding),
            colors = TextFieldDefaults.textFieldColors(
                textColor = themeColors.onBackground,
            ),
        )
    }
}

/**
 * Column
 * 1. checkbox for case sensitivity
 * 2. radio buttons for filter type
 * 3. delete button
 * 4. collapse section button
 */
@Composable
@ExperimentalMaterialApi
private fun filterProperties(
    filterViewModel:FilterViewModel,
    themeColors:Colors,

    /**
     * callback to request collapsing the section.
     * this is intended to be used by the host to update the expanded state of the section.
     */
    onCollapseSection:()->Unit,
)
{
    Column()
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
        Text("Filter type",modifier = Modifier.padding(start = Dimens.mttSize))
        val filterTypeFlow by filterViewModel.filterInterpretationModeFlow.collectAsState(null)
        for (filterType in FilterInterpretationMode.entries)
        {
            val onClick = { filterViewModel.setFilterType(filterType) }
            Surface(
                modifier = Modifier.fillMaxWidth().padding(start = Dimens.mttSize),
                shape = MaterialTheme.shapes.small,
                onClick = onClick,
            )
            {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                )
                {
                    RadioButton(
                        selected = filterTypeFlow == filterType,
                        onClick = onClick,
                    )
                    Text(filterType.displayName)
                }
            }
        }

        // spacing
        Spacer(modifier = Modifier.size(Dimens.mttPadding))

        // delete button
        DoubleClickButton(
            modifier = Modifier.fillMaxWidth().padding(start = Dimens.mttSize),
            onDoubleClick = { filterViewModel.requestDelete() },
            idleButtonColors = ButtonDefaults.buttonColors(themeColors.error),
            idleContent = { Text("Delete filter") },
            clickedOnceButtonColors = ButtonDefaults.buttonColors(themeColors.error),
            clickedOnceContent = { Text("Really delete filter?") },
        )

        // collapse section button
        Surface(
            modifier = Modifier.fillMaxWidth().padding(start = Dimens.mttSize),
            shape = MaterialTheme.shapes.small,
            onClick = onCollapseSection,
        )
        {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(Dimens.mttPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            )
            {
                IconCollapseSection(themeColors.onSurface)
            }
        }
    }
}

val FilterInterpretationMode.displayName:String get() = when (this){
    FilterInterpretationMode.STRING_LITERAL -> "Plaintext"
    FilterInterpretationMode.REGULAR_EXPRESSION -> "Regex"
    FilterInterpretationMode.LOGCAT_FILTER -> "Logcat filter"
}

