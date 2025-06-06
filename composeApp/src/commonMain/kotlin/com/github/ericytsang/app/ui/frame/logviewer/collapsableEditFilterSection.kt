

package com.github.ericytsang.app.ui.frame.logviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.util.component.ToggleButton

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
     * composable to be used as the icon for the section header.
     * this is intended to be used by the host to provide a custom icon for the section header.
     */
    sectionIcon:@Composable (contentColor:Color)->Unit,

    /**
     * whether this section is expanded or not.
     * if it is expanded, the filter builder panel is shown.
     */
    isSectionExpanded:Boolean,

    /**
     * callback to request toggling the section expanded state.
     * this is intended to be used by the host to update [isSectionExpanded].
     */
    requestToggleSectionExpanded:() -> Unit,

    /**
     * list of filters to be displayed in the filter builder panel.
     * each filter is represented by a [FilterViewModelImpl].
     */
    placeholderFilters:List<FilterViewModelImpl>,

    /**
     * the id of the filter that is currently expanded.
     * this is used to determine whether to show the additional settings for a filter.
     */
    expandedFilterId:FilterId,

    /**
     * callback when the text field gets focus.
     * this is intended to be used by the host to update [expandedFilterId].
     */
    requestFilterExpansion:(FilterId) -> Unit,
)
{
    item(key = "$lazyColumnItemKeyPrefix-header") {
        Row(modifier = Modifier.animateItem().background(themeColors.background)) {
            ToggleButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = requestToggleSectionExpanded,
                isToggled = isSectionExpanded,
                isToggledColors = ButtonDefaults.buttonColors(themeColors.primary),
                isNotToggledColors = ButtonDefaults.buttonColors(themeColors.surface),
                content = sectionIcon,
            )
        }
    }

    if (isSectionExpanded)
    {
        filterBuilderPanel(
            themeColors = themeColors,
            lazyColumnItemKeyPrefix = "$lazyColumnItemKeyPrefix-panel",
            filterViewModels = placeholderFilters,
            expandedItem = expandedFilterId,
            onTextFieldGotFocus = requestFilterExpansion,
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
@ExperimentalMaterialApi
fun LazyListScope.filterBuilderPanel(

    /**
     * colors to be used for the UI components.
     * this is used to ensure that the UI components are consistent with the theme.
     */
    themeColors:Colors,

    /**
     * [LazyColumn] uses keys to keep track of its contained items
     * so it can infer what animations to perform as items CRUD
     */
    lazyColumnItemKeyPrefix:String,

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
    for (filterViewModel in filterViewModels)
    {
        // checkbox for enable/disable the filter + text field for filter string
        item(key = "$lazyColumnItemKeyPrefix-${filterViewModel.filterId.id}-header")
        {
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
            Row(
                modifier = Modifier.animateItem().background(themeColors.background),
                verticalAlignment = Alignment.CenterVertically,
            )
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
        }

        // show the additional settings for this filter if it is expanded ([expandedItem])
        if (filterViewModel.filterId == expandedItem)
        {
            item(key = "$lazyColumnItemKeyPrefix-${filterViewModel.filterId.id}-settings")
            {
                Column(Modifier.fillMaxWidth().animateItem().background(themeColors.background))
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
