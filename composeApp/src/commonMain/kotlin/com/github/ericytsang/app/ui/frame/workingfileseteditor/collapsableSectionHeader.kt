package com.github.ericytsang.app.ui.frame.workingfileseteditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Colors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.frame.logviewer.ReorderableSidebarItemKey
import com.github.ericytsang.app.ui.util.component.ToggleButton

@ExperimentalMaterialApi
fun LazyListScope.collapsableSectionHeader(

    /** colors to be used for the UI components. */
    themeColors:Colors,

    /**
     * [androidx.compose.foundation.lazy.LazyColumn] uses keys to keep track of its contained items
     * so it can infer what animations to perform as items CRUD
     */
    lazyColumnItemKeyPrefix:String,

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
)
{
    // header for the section
    item(key = ReorderableSidebarItemKey.Other("$lazyColumnItemKeyPrefix-header"))
    {
        Row(modifier = Modifier.Companion.animateItem().background(themeColors.background))
        {
            ToggleButton(
                modifier = Modifier.Companion.fillMaxWidth(),
                onClick = requestToggleSectionExpanded,
                isToggled = isSectionExpanded,
                isToggledColors = ButtonDefaults.buttonColors(themeColors.primary),
                isNotToggledColors = ButtonDefaults.buttonColors(themeColors.surface),
                content =
                { contentColor ->
                    Row(
                        modifier = Modifier.Companion.fillMaxWidth(),
                        verticalAlignment = Alignment.Companion.CenterVertically,
                    )
                    {
                        sectionIcon(contentColor)
                        if (shouldShowSectionHeader)
                        {
                            Text(
                                text = sectionTitle,
                                color = contentColor,
                                modifier = Modifier.Companion.padding(start = Dimens.mttPadding),
                            )
                        }
                    }
                },
            )
        }
    }
}