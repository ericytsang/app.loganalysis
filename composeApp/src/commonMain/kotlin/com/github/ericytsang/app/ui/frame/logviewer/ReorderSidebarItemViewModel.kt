package com.github.ericytsang.app.ui.frame.logviewer

import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.FilterRepository
import com.github.ericytsang.domain.repo.repo.WorkingFileSetRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.withContext

interface ReorderSidebarItemViewModel
{
    suspend fun moveItem(from:ReorderableSidebarItemKey,to:ReorderableSidebarItemKey)

    companion object
    {
        fun create(
            configurationId:ConfigurationId,
        ):ReorderSidebarItemViewModel = ReorderSidebarItemViewModelImpl(
            configurationId = configurationId,
        )
    }
}

class ReorderSidebarItemViewModelImpl(
    private val configurationId:ConfigurationId,
    private val workingFileSetRepo:WorkingFileSetRepository = RepositoryDependencyProvider.instance.workingFileSetRepository,
    private val filterRepo:FilterRepository = RepositoryDependencyProvider.instance.filterRepository,
    private val kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
):ReorderSidebarItemViewModel,KotlinDependencyProvider by kotlinDependencyProvider
{
    override suspend fun moveItem(from:ReorderableSidebarItemKey,to:ReorderableSidebarItemKey) =
        withContext(dispatchers.io)
        {
            when (from)
            {
                // non reorderable item, do nothing
                is ReorderableSidebarItemKey.Other -> return@withContext

                // "new filter" button can only be used as a target, and cannot be moved
                is ReorderableSidebarItemKey.NewFilterButton -> error("cannot move 'new filter' button")

                is ReorderableSidebarItemKey.FilterItem -> when (to)
                {
                    // non reorderable item, do nothing
                    is ReorderableSidebarItemKey.Other -> return@withContext

                    // not allowed to reorder with log file item
                    is ReorderableSidebarItemKey.LogFileItem -> return@withContext

                    // move filter to the target position
                    is ReorderableSidebarItemKey.FilterItem -> filterRepo.moveFilter(from.filterId,to.filterId)

                    // move filter to the top of the section
                    is ReorderableSidebarItemKey.NewFilterButton ->
                        filterRepo.moveFilterToTopOfSection(from.filterId,to.filterType)
                }

                is ReorderableSidebarItemKey.LogFileItem -> when (to)
                {
                    // non reorderable item, do nothing
                    is ReorderableSidebarItemKey.Other -> return@withContext

                    // not allowed to reorder with filter item
                    is ReorderableSidebarItemKey.FilterItem -> return@withContext

                    // not allowed to reorder with "new filter" button
                    is ReorderableSidebarItemKey.NewFilterButton -> return@withContext

                    // move log file to the target position
                    is ReorderableSidebarItemKey.LogFileItem ->
                        workingFileSetRepo.moveFile(configurationId,from.filePath,to.filePath)
                }
            }
        }
}