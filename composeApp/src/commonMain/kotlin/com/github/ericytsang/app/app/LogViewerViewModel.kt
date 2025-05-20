package com.github.ericytsang.app.app

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

    val activeFilterParsed:Flow<Node> = _logcatFilterString
        .mapLatest { filter -> logcatFilterParser.parse(filter) }
        .flowOn(dispatchers.io)

    fun getLogLinesFlow():Flow<List<String>> = concatenatedFiles
        .mapLatest { files -> files.flatMap { file -> file.readLines() } }
        .combine(activeFilterParsed, ::applyFilterToLogLines)
        .flowOn(dispatchers.io)

    private fun applyFilterToLogLines(
        logLines:List<String>,
        logcatFilter:Node,
    ):List<String> = logLines.filter { logLine ->
        logcatFilterEvaluator.isMatch(logLine, logcatFilter)
    }
}