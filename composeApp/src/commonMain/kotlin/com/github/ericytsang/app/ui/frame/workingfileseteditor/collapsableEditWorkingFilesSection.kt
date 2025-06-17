package com.github.ericytsang.app.ui.frame.workingfileseteditor

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Colors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import sh.calvin.reorderable.ReorderableLazyListState
import java.io.File

interface WorkingFileViewModel
{
    val file:File
    fun requestDelete()
}

@ExperimentalMaterialApi
fun LazyListScope.collapsableEditWorkingFilesSection(

    /** colors to be used for the UI components. */
    themeColors:Colors,

    /**
     * [androidx.compose.foundation.lazy.LazyColumn] uses keys to keep track of its contained items
     * so it can infer what animations to perform as items CRUD
     */
    lazyColumnItemKeyPrefix:String,

    /**
     * [sh.calvin.reorderable.ReorderableLazyListState] to be used when creating reorderable items.
     * this is used to allow drag-and-drop reordering of the filters.
     */
    reorderableLazyListState:ReorderableLazyListState,

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
     * each filter is represented by a [com.github.ericytsang.app.ui.frame.logviewer.FilterViewModel].
     */
    itemViewModels:List<WorkingFileViewModel>,
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
}