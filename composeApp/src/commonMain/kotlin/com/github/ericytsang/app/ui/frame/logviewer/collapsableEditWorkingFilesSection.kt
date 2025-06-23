package com.github.ericytsang.app.ui.frame.logviewer

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Colors
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.util.asset.IconDragHandle
import com.github.ericytsang.app.ui.util.asset.IconMoreOptions
import com.github.ericytsang.app.ui.util.component.DoubleClickButton
import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.domain.objects.FilePath
import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.WorkingFileSetRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState

interface LogFileViewModel
{
    val filePath:FilePath
    fun requestDelete()
}

interface LogFileListViewModel
{
    val itemViewModels:Flow<List<LogFileViewModel>>
}

class LogFileListViewModelImpl(
    private val configurationId:ConfigurationId,
    private val workingFileSetRepository:WorkingFileSetRepository = RepositoryDependencyProvider.instance.workingFileSetRepository,
    private val kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
):LogFileListViewModel, KotlinDependencyProvider by kotlinDependencyProvider
{
    override val itemViewModels:Flow<List<LogFileViewModel>> = workingFileSetRepository
        .getWorkingFileSetFlow(configurationId)
        .map {
            it.files.map { file ->
                object : LogFileViewModel
                {
                    override val filePath = file.filePath
                    override fun requestDelete()
                    {
                        applicationScope.launch(dispatchers.io)
                        {
                            workingFileSetRepository.removeFile(configurationId,file.filePath)
                        }
                    }
                }
            }
        }
        .conflate()
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
     * [ReorderableLazyListState] to be used when creating reorderable items.
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
     * each filter is represented by a [FilterViewModel].
     */
    itemViewModels:List<LogFileViewModel>,

    /**
     * invoked when the user presses the button to add more files to this configuration.
     */
    requestAddNewWorkingFile:() -> Unit,
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
        val itemKey = ReorderableSidebarItemKey.Other("$lazyColumnItemKeyPrefix-header-add_new_file_button")
        item(key = itemKey)
        {
            Row(modifier = Modifier.animateItem().background(themeColors.background))
            {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth().animateItem().padding(Dimens.mttPadding),
                    onClick = requestAddNewWorkingFile,
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = themeColors.surface,
                        contentColor = themeColors.onSurface,
                    ),
                )
                {
                    Text("Add files")
                }
            }
        }

        // region show the filters that the user can edit

        val filePaths = itemViewModels.map { it.filePath.filePath }
        val commonPrefix = when (val firstFile = filePaths.firstOrNull())
        {
            null -> ""
            else -> filePaths.fold(firstFile) { acc,path -> acc.commonPrefixWith(path) }
        }

        // show the common prefix in its own item
        item(key = ReorderableSidebarItemKey.Other("$lazyColumnItemKeyPrefix-header-common_prefix"))
        {
            val secondaryTextColor = themeColors.onBackground.copy(alpha = ContentAlpha.medium)
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = secondaryTextColor)) { append("$commonPrefix...") }
                },
                modifier = Modifier.fillMaxWidth().padding(Dimens.mttPadding),
                color = themeColors.onBackground.copy(alpha = ContentAlpha.medium),
            )
        }

        // show each file path as a reorderable item
        items(
            count = itemViewModels.size,
            key = { index -> ReorderableSidebarItemKey.LogFileItem(itemViewModels[index].filePath) },
        )
        { index ->
            val itemViewModel = itemViewModels[index]
            val itemKey = ReorderableSidebarItemKey.LogFileItem(itemViewModel.filePath)
            ReorderableItem(
                key = itemKey,
                state = reorderableLazyListState,
            )
            { isDragging ->

                // increase elevation during dragging
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                Surface(elevation = elevation)
                {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(themeColors.background)
                            .padding(Dimens.mttPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.mttPadding),
                    )
                    {
                        // drag-and-drop handle
                        IconButton(
                            modifier = Modifier.draggableHandle(),
                            onClick = {},
                        )
                        {
                            IconDragHandle(themeColors.onSurface)
                        }

                        // file path text
                        Text(
                            modifier = Modifier.weight(1f, fill = true),
                            // if the texts have a common prefix, then make the common prefix portion the secondary text color
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold))
                                {
                                    append(itemViewModel.filePath.filePath.removePrefix(commonPrefix))
                                }
                            }
                        )

                        // delete button
                        DoubleClickButton(
                            onDoubleClick = { itemViewModel.requestDelete() },
                            idleButtonColors = ButtonDefaults.buttonColors(themeColors.surface),
                            idleContent = { IconMoreOptions(themeColors.onSurface) },
                            clickedOnceButtonColors = ButtonDefaults.buttonColors(themeColors.error),
                            clickedOnceContent = { Text("Delete") },
                        )
                    }
                }
            }
        }

        // endregion
    }
}