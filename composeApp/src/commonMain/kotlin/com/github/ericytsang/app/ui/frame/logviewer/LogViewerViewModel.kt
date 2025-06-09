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
import kotlinx.coroutines.flow.filter
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

    // region logcat filter string

    val logcatFilterString:StateFlow<String> get() = _logcatFilterString
    private val _logcatFilterString = MutableStateFlow("")

    fun setLogcatFilterString(filter:String)
    {
        _logcatFilterString.value = filter
    }

    sealed class ParsedLogcatFilter
    {
        data class Parsed(val node:Node):ParsedLogcatFilter()
        data object Empty:ParsedLogcatFilter()
        data object Error:ParsedLogcatFilter()
    }

    val activeFilterParsed:Flow<ParsedLogcatFilter> = _logcatFilterString
        .mapLatest { filter ->
            when
            {
                filter.isBlank() -> ParsedLogcatFilter.Empty
                else -> runCatching { logcatFilterParser.parse(filter) }.fold(
                    onSuccess = { ParsedLogcatFilter.Parsed(it) },
                    onFailure = { ParsedLogcatFilter.Error },
                )
            }
        }
        .flowOn(dispatchers.io)

    // endregion

    // region case sensitivity

    val isCaseSensitive:StateFlow<Boolean> get() = _isCaseSensitive
    private val _isCaseSensitive = MutableStateFlow(false)

    fun setCaseSensitive(isCaseSensitive:Boolean)
    {
        _isCaseSensitive.value = isCaseSensitive
    }

    // endregion

    // region get activated sidebar filters

    private val activeFilters:Flow<List<Node>> = filterRepository
        .selectActiveFiltersForConfig(configurationId)
        .map { list -> list.filter { it.filterString.isNotEmpty() } }
        .map { list -> list.map { toFilterNode(it) } }
        .flowOn(dispatchers.io)
        .conflate()

    private fun toFilterNode(filterModel:FilterModel):Node
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
            FilterInterpretationMode.LOGCAT_FILTER -> logcatFilterParser.parse(
                input = filterModel.filterString,
                forceIsCaseSensitive = filterModel.isCaseSensitive,
            )
        }
        return when (filterModel.filterType)
        {
            FilterType.INCLUDE -> filterNode
            FilterType.EXCLUDE -> NotNode(filterNode)
        }
    }

    // endregion

    // region log lines

    private val fileLines = concatenatedFiles
        .mapLatest { files -> files.flatMap { file -> file.readLines() } }
        .conflate()

    fun getLogLinesFlow():Flow<List<String>> =
        combine(
            fileLines,
            activeFilterParsed,
            isCaseSensitive,
            activeFilters,
            ::applyFilterToLogLines,
        ).flowOn(dispatchers.io)

    private fun applyFilterToLogLines(
        logLines:List<String>,
        logcatFilter:ParsedLogcatFilter,
        isCaseSensitive:Boolean,
        activeFilters:List<Node>,
    ):List<String> = logLines.filter { logLine ->
        val isMatchWithMasterFilter = when (logcatFilter)
        {
            is ParsedLogcatFilter.Parsed -> logcatFilterEvaluator.isMatch(
                caseSensitive = isCaseSensitive,
                logLine = logLine,
                logcatFilter = logcatFilter.node,
            )
            ParsedLogcatFilter.Empty -> true
            ParsedLogcatFilter.Error -> false
        }

        isMatchWithMasterFilter && activeFilters.all { filter ->
            logcatFilterEvaluator.isMatch(
                caseSensitive = false,
                logLine = logLine,
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