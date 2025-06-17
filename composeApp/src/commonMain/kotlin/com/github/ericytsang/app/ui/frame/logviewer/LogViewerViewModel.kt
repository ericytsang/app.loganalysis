package com.github.ericytsang.app.ui.frame.logviewer

import com.github.ericytsang.app.util.LogcatFilterEvaluator
import com.github.ericytsang.app.util.LogcatFilterEvaluatorImpl
import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.domain.objects.FilterInterpretationMode
import com.github.ericytsang.domain.objects.FilterModel
import com.github.ericytsang.domain.objects.FilterType
import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.FilterRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.logcatfilterparser.LeafNode
import com.github.ericytsang.logcatfilterparser.LogcatFilterParser
import com.github.ericytsang.logcatfilterparser.Node
import com.github.ericytsang.logcatfilterparser.NotNode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class LogViewerViewModel(
    private val logcatFilterParser:LogcatFilterParser,
    private val logcatFilterEvaluator:LogcatFilterEvaluator,
    configurationId:ConfigurationId,
    kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
    filterRepository:FilterRepository = RepositoryDependencyProvider.instance.filterRepository,
):KotlinDependencyProvider by kotlinDependencyProvider
{
    init
    {
        println("LogViewerViewModel created")
    }

    // region concatenated files

    val concatenatedFiles:StateFlow<List<File>> get() = _concatenatedFiles
    private val _concatenatedFiles = MutableStateFlow(emptyList<File>())

    fun setConcatenatedFiles(files:List<File>)
    {
        _concatenatedFiles.value = files
    }

    // endregion

    // region get activated sidebar filters

    private val activeFilters:Flow<List<Node>> = filterRepository
        .selectActiveFiltersForConfig(configurationId)
        .map { list -> list.filter { it.filterString.isNotEmpty() } }
        .map { list -> list.mapNotNull { toFilterNode(it) } }
        .flowOn(dispatchers.io)
        .conflate()

    private fun toFilterNode(filterModel:FilterModel):Node?
    {
        val filterNode = when (filterModel.filterInterpretationMode)
        {
            FilterInterpretationMode.STRING_LITERAL -> LeafNode(
                key = "",
                value = filterModel.filterString,
                regex = false,
                caseSensitive = filterModel.isCaseSensitive,
            )
            FilterInterpretationMode.REGULAR_EXPRESSION -> LeafNode(
                key = "",
                value = filterModel.filterString,
                regex = true,
                caseSensitive = filterModel.isCaseSensitive,
            )
            FilterInterpretationMode.LOGCAT_FILTER -> try
            {
                logcatFilterParser.parse(
                    input = filterModel.filterString,
                    forceIsCaseSensitive = filterModel.isCaseSensitive,
                )
            }
            catch (e:Exception)
            {
                println("Error parsing logcat filter: ${filterModel.filterString}, error: ${e.message}")
                null
            }
        }
        return when
        {
            filterNode == null -> null
            else -> when (filterModel.filterType)
            {
                FilterType.INCLUDE -> filterNode
                FilterType.EXCLUDE -> NotNode(filterNode)
            }
        }
    }

    // endregion

    // region log lines

    private val fileLines:Flow<List<IndexedValue<String>>> = concatenatedFiles
        .mapLatest { files -> files.flatMap { file -> file.readLines() } }
        .mapLatest { lines -> lines.withIndex().toList() }
        .conflate()

    fun getLogLinesFlow():Flow<List<IndexedValue<String>>> =
        combine(
            fileLines,
            activeFilters,
            ::applyFilterToLogLines,
        ).flowOn(dispatchers.io)

    private fun applyFilterToLogLines(
        logLines:List<IndexedValue<String>>,
        activeFilters:List<Node>,
    ):List<IndexedValue<String>> = logLines.filter { logLine ->
        activeFilters.all { filter ->
            logcatFilterEvaluator.isMatch(
                logLine = logLine.value,
                logcatFilter = filter,
            )
        }
    }

    // endregion

    companion object
    {
        fun create(
            configurationId:ConfigurationId,
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
            logcatFilterParser:LogcatFilterParser = LogcatFilterParser(),
            logcatFilterEvaluator:LogcatFilterEvaluator = LogcatFilterEvaluatorImpl(),
        ):LogViewerViewModel = LogViewerViewModel(
            kotlinDependencyProvider = kotlinDependencyProvider,
            logcatFilterParser = logcatFilterParser,
            logcatFilterEvaluator = logcatFilterEvaluator,
            configurationId = configurationId,
        )
    }
}