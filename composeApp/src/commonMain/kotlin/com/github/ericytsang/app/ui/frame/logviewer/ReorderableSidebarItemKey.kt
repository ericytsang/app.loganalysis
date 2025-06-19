package com.github.ericytsang.app.ui.frame.logviewer

import com.github.ericytsang.domain.objects.FilePath
import com.github.ericytsang.domain.objects.FilterId
import com.github.ericytsang.domain.objects.FilterType

sealed interface ReorderableSidebarItemKey
{
    /** key for non reorderable item */
    data class Other(val key:String):ReorderableSidebarItemKey

    /**
     * key for the "new filter" button which will be treated as the user
     * trying to add the filter to the top of the section.
     */
    data class NewFilterButton(val filterType:FilterType):ReorderableSidebarItemKey

    /** key for reorderable filter item */
    data class FilterItem(val filterId:FilterId):ReorderableSidebarItemKey

    /** key for reorderable log file item */
    data class LogFileItem(val filePath:FilePath):ReorderableSidebarItemKey
}