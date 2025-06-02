package com.github.ericytsang.app.ui.frame.projectbrowser

import com.github.ericytsang.domain.objects.Configuration
import com.github.ericytsang.domain.objects.ConfigurationUpdateSequence
import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.ConfigurationRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlin.time.Duration.Companion.seconds

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
    fun setMaxInMemoryItemsCountHint(count:Int)

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
    val updateSequence:ConfigurationUpdateSequence
}

sealed class ProjectListItemModel
{
    data class Project(
        val configuration:Configuration,
    ):ProjectListItemModel()

    data class LazyLoadMoreItemsBelow(
        override val updateSequence:ConfigurationUpdateSequence,
    ):ProjectListItemModel(),LoadMoreItemsRequest

    data class LazyLoadMoreItemsAbove(
        override val updateSequence:ConfigurationUpdateSequence,
    ):ProjectListItemModel(),LoadMoreItemsRequest
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class ProjectBrowserViewModelImpl(
    private val kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
    private val configurationRepository:ConfigurationRepository = RepositoryDependencyProvider.instance.configurationRepository,
):ProjectBrowserViewModel,
    KotlinDependencyProvider by kotlinDependencyProvider
{
    private val maxInMemoryItemsCountHint = MutableStateFlow(2)

    private val loadParamsFlow = MutableStateFlow<LoadMoreItemsParams>(
        LoadMoreItemsParams(
            loadPosition = DEFAULT_UPDATE_SEQUENCE,
            loadDirection = LoadDirection.BELOW,
        )
    )

    private data class LoadMoreItemsParams(
        val loadPosition:ConfigurationUpdateSequence,
        val loadDirection:LoadDirection,
    )

    enum class LoadDirection
    {
        ABOVE, // load more items above the current items
        BELOW, // load more items below the current items
    }

    private val onProjectsChangedFlow = configurationRepository.getOnChangedFlow()

    private fun rangeToProjectsFlow():Flow<List<ProjectListItemModel>> = combine(maxInMemoryItemsCountHint, loadParamsFlow)
    { maxInMemoryItemsCountHintValue, loadParams ->
        val loadPosition = loadParams.loadPosition
        val loadDirection = loadParams.loadDirection
        coroutineScope {
            delay(2.seconds)
            val loadedOnDemand = async {
                when (loadDirection)
                {
                    LoadDirection.BELOW -> configurationRepository.loadNextNConfigurationIdsBefore(AMOUNT_TO_LOAD_ON_DEMAND,loadPosition)
                    LoadDirection.ABOVE -> configurationRepository.loadNextNConfigurationIdsAfter(AMOUNT_TO_LOAD_ON_DEMAND,loadPosition)
                }
            }
            val reloadingExisting = async {
                val offsetLoadPosition = when (loadDirection)
                {
                    LoadDirection.BELOW -> ConfigurationUpdateSequence(loadPosition.updateSequence - 1)
                    LoadDirection.ABOVE -> ConfigurationUpdateSequence(loadPosition.updateSequence + 1)
                }
                when (loadDirection)
                {
                    LoadDirection.BELOW -> configurationRepository.loadNextNConfigurationIdsAfter(maxInMemoryItemsCountHintValue,offsetLoadPosition)
                    LoadDirection.ABOVE -> configurationRepository.loadNextNConfigurationIdsBefore(maxInMemoryItemsCountHintValue,offsetLoadPosition)
                }
            }
            val hasMoreItemsAbove = when (loadDirection)
            {
                LoadDirection.BELOW -> reloadingExisting.await().size >= maxInMemoryItemsCountHintValue
                LoadDirection.ABOVE -> loadedOnDemand.await().size >= AMOUNT_TO_LOAD_ON_DEMAND
            }
            val hasMoreItemsBelow = when (loadDirection)
            {
                LoadDirection.BELOW -> loadedOnDemand.await().size >= AMOUNT_TO_LOAD_ON_DEMAND
                LoadDirection.ABOVE -> reloadingExisting.await().size >= maxInMemoryItemsCountHintValue
            }
            val items = when (loadDirection)
            {
                LoadDirection.BELOW -> reloadingExisting.await()+loadedOnDemand.await()
                LoadDirection.ABOVE -> loadedOnDemand.await()+reloadingExisting.await()
            }
            buildList()
            {
                if (hasMoreItemsAbove)
                {
                    add(ProjectListItemModel.LazyLoadMoreItemsAbove(items.firstOrNull()?.updateSequence ?: DEFAULT_UPDATE_SEQUENCE))
                }
                addAll(items.map { ProjectListItemModel.Project(it) })
                if (hasMoreItemsBelow)
                {
                    add(ProjectListItemModel.LazyLoadMoreItemsBelow(items.lastOrNull()?.updateSequence ?: DEFAULT_UPDATE_SEQUENCE))
                }
            }
        }
    }

    override val getProjectsFlow:Flow<List<ProjectListItemModel>> = onProjectsChangedFlow
        .flatMapLatest { rangeToProjectsFlow() }
        .flowOn(dispatchers.io)

    override fun setMaxInMemoryItemsCountHint(count:Int)
    {
        maxInMemoryItemsCountHint.value = count
    }

    override fun loadMoreItems(request:LoadMoreItemsRequest)
    {
        when (request)
        {
            is ProjectListItemModel.LazyLoadMoreItemsAbove ->
            {
                loadParamsFlow.value = LoadMoreItemsParams(
                    loadPosition = request.updateSequence,
                    loadDirection = LoadDirection.ABOVE,
                )
            }
            is ProjectListItemModel.LazyLoadMoreItemsBelow ->
            {
                loadParamsFlow.value = LoadMoreItemsParams(
                    loadPosition = request.updateSequence,
                    loadDirection = LoadDirection.BELOW,
                )
            }
        }
    }

    companion object
    {
        private const val AMOUNT_TO_LOAD_ON_DEMAND = 1
        private val DEFAULT_UPDATE_SEQUENCE = ConfigurationUpdateSequence(Long.MAX_VALUE)
    }
}