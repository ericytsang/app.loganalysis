package com.github.ericytsang.app.ui.frame.app

import com.github.ericytsang.app.util.LogcatFilterEvaluator
import com.github.ericytsang.app.util.LogcatFilterEvaluatorImpl
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.logcatfilterparser.LogcatFilterParser
import com.github.ericytsang.logcatfilterparser.Node
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class LogViewerViewModel(
    private val kotlinDependencyProvider:KotlinDependencyProvider,
    private val logcatFilterParser:LogcatFilterParser,
    private val logcatFilterEvaluator:LogcatFilterEvaluator,
):KotlinDependencyProvider by kotlinDependencyProvider
{
    init
    {
        println("LogViewerViewModel created")
    }

    val concatenatedFiles:StateFlow<List<File>> get() = _concatenatedFiles
    private val _concatenatedFiles = MutableStateFlow(emptyList<File>())

    fun setConcatenatedFiles(files:List<File>)
    {
        _concatenatedFiles.value = files
    }

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

    fun getLogLinesFlow():Flow<List<String>> = concatenatedFiles
        .mapLatest { files -> files.flatMap { file -> file.readLines() } }
        .combine(activeFilterParsed, ::applyFilterToLogLines)
        .flowOn(dispatchers.io)

    private fun applyFilterToLogLines(
        logLines:List<String>,
        logcatFilter:ParsedLogcatFilter,
    ):List<String> = logLines.filter { logLine ->
        when (logcatFilter)
        {
            is ParsedLogcatFilter.Parsed -> logcatFilterEvaluator.isMatch(logLine, logcatFilter.node)
            ParsedLogcatFilter.Empty -> true
            ParsedLogcatFilter.Error -> false
        }
    }

    companion object
    {
        fun createDefault(
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.Companion.instance,
            logcatFilterParser:LogcatFilterParser = LogcatFilterParser(),
            logcatFilterEvaluator:LogcatFilterEvaluator = LogcatFilterEvaluatorImpl(),
        ):LogViewerViewModel = LogViewerViewModel(
            kotlinDependencyProvider = kotlinDependencyProvider,
            logcatFilterParser = logcatFilterParser,
            logcatFilterEvaluator = logcatFilterEvaluator,
        )
    }
}