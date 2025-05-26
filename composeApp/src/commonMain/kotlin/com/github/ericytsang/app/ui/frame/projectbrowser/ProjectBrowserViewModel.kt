package com.github.ericytsang.app.ui.frame.projectbrowser

import com.github.ericytsang.app.usecase.ThemeUseCase
import com.github.ericytsang.domain.objects.Configuration
import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.ConfigurationRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

/**
 * ViewModel for the Project Browser window.
 * it supports the following features:
 * - displaying a history of concatenated log files
 * - it is sorted by time, with most recent at the top
 * - user can press on a trash can icon on the item to delete that project
 * - user can press on a open icon on the item to open that project in a log viewer window
 * - user can press on an edit icon to open the working file set editor window
 */
interface ProjectBrowserViewModel
{
    val getProjectsFlow:Flow<List<ProjectListItemModel>>

    /**
     * for the UI to communicate to the ViewModel the number of items that are currently visible in the UI.
     * is this for helping with pagination, so the ViewModel can determine how many items to load from the
     * database into the UI.
     */
    fun setMaxInMemoryItemsCount(count:Int)

    /**
     * this is for the UI to communicate to the ViewModel that it needs more items to be loaded.
     * the ViewModel will asynchronously load more items and update the [getProjectsFlow] with the new items.
     * the ViewModel may also remove items on the opposite end of the list if the number of items exceeds the
     * visible items count (plus some buffer).
     */
    fun loadMoreItems(request:LoadMoreItemsRequest)

    companion object
    {
        fun create(
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
            configurationRepository:ConfigurationRepository = RepositoryDependencyProvider.instance.configurationRepository,
        ):ProjectBrowserViewModel = ProjectBrowserViewModelImpl(
            kotlinDependencyProvider = kotlinDependencyProvider,
            configurationRepository = configurationRepository,
        )
    }
}

sealed interface LoadMoreItemsRequest
{
    val requestId: String
}

sealed class ProjectListItemModel
{
    data class Project(
        val configuration:Configuration,
    ):ProjectListItemModel()

    data class LazyLoadMoreItemsBelow(
        override val requestId: String,
    ):ProjectListItemModel(),LoadMoreItemsRequest

    data class LazyLoadMoreItemsAbove(
        override val requestId: String,
    ):ProjectListItemModel(),LoadMoreItemsRequest
}

internal class ProjectBrowserViewModelImpl(
    private val kotlinDependencyProvider:KotlinDependencyProvider,
    private val configurationRepository:ConfigurationRepository,
):ProjectBrowserViewModel,
    KotlinDependencyProvider by kotlinDependencyProvider
{
    private val maxInMemoryItemsCount = MutableStateFlow(200)

    override val getProjectsFlow:Flow<List<ProjectListItemModel>> = flow()
    {
        val items = configurationRepository.loadNextNConfigurationIdsAfter(100, Long.MIN_VALUE)
        emit(items.map { ProjectListItemModel.Project(it) })
        awaitCancellation()
    }.flowOn(dispatchers.io)

    override fun setMaxInMemoryItemsCount(count:Int)
    {
        maxInMemoryItemsCount.value = count
    }

    override fun loadMoreItems(request:LoadMoreItemsRequest)
    {
        TODO("Not yet implemented")
    }
}