

package com.github.ericytsang.app.ui.frame.logviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.github.ericytsang.app.ui.util.component.ToggleButton

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