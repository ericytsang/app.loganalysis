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

    private val fileLines:Flow<List<FileLine>> = concatenatedFiles
        .map { files ->
            files
                .map { file -> FileLines(file,file.readLines().withIndex().toList()) }
                .flatMap { fileLines ->
                    fileLines.lines.map { line ->
                        val key = FileLineKey(
                            file = fileLines.file,
                            lineNumberInFile = line.index + 1,
                        )
                        FileLine(
                            key = key,
                            line = line.value,
                        )
                    }
                }
        }
        .conflate()

    data class FileLines(
        val file:File,
        val lines:List<IndexedValue<String>>,
    )

    data class FileLine(
        val key:FileLineKey,
        val line:String,
    )

    data class FileLineKey(
        val file:File,
        val lineNumberInFile:Int,
    )

    fun getLogLinesFlow():Flow<List<FileLine>> =
        combine(
            fileLines,
            activeFilters,
            ::applyFilterToLogLines,
        ).flowOn(dispatchers.io)

    private fun applyFilterToLogLines(
        logLines:List<FileLine>,
        activeFilters:List<Node>,
    ):List<FileLine> = logLines.filter { logLine ->
        activeFilters.all { filter ->
            logcatFilterEvaluator.isMatch(
                logLine = logLine.line,
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