package com.github.ericytsang.app.ui.frame.logviewer

import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import com.github.ericytsang.app.util.LogcatFilterEvaluator
import com.github.ericytsang.app.util.LogcatFilterEvaluatorImpl
import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.domain.objects.FilePath
import com.github.ericytsang.domain.objects.FilePath.Companion.toFilePath
import com.github.ericytsang.domain.objects.FilterInterpretationMode
import com.github.ericytsang.domain.objects.FilterModel
import com.github.ericytsang.domain.objects.FilterType
import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.FilterRepository
import com.github.ericytsang.kotlin.ImmutableCoroutineScope
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.kotlin.newChildScope
import com.github.ericytsang.logcatfilterparser.LeafNode
import com.github.ericytsang.logcatfilterparser.LogcatFilterParser
import com.github.ericytsang.logcatfilterparser.Node
import com.github.ericytsang.logcatfilterparser.NotNode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import java.awt.datatransfer.StringSelection
import java.io.File
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class LogViewerViewModel(
    private val uiScope:ImmutableCoroutineScope,
    private val logcatFilterParser:LogcatFilterParser,
    private val logcatFilterEvaluator:LogcatFilterEvaluator,
    configurationId:ConfigurationId,
    kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
    filterRepository:FilterRepository = RepositoryDependencyProvider.instance.filterRepository,
):KotlinDependencyProvider by kotlinDependencyProvider
{
    // region concatenated files

    val concatenatedFilesFlow:StateFlow<List<File>> get() = _concatenatedFiles
    private val _concatenatedFiles = MutableStateFlow(emptyList<File>())

    fun setConcatenatedFiles(files:List<File>)
    {
        _concatenatedFiles.value = files
    }

    // endregion

    // region get activated sidebar filters

    private val activeFilters:Flow<Map<FilterType, List<Node>>> = filterRepository
        .selectActiveFiltersForConfig(configurationId)
        .map { list -> list.filter { it.filterString.isNotEmpty() } }
        .map { list -> list.mapNotNull { toFilterNode(it) } }
        .map { stillNeedToFolds -> stillNeedToFolds.groupBy({ it.filterType },{ it.filterNode }) }
        .flowOn(dispatchers.io)
        .conflate()

    private fun toFilterNode(filterModel:FilterModel):FilterModelWithNode?
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
                FilterType.INCLUDE,FilterType.BOOKMARK -> FilterModelWithNode(
                    filterType = filterModel.filterType,
                    filterNode = filterNode,
                )
                FilterType.EXCLUDE -> FilterModelWithNode(
                    filterType = filterModel.filterType,
                    filterNode = NotNode(filterNode),
                )
            }
        }
    }

    data class FilterModelWithNode(
        val filterType:FilterType,
        val filterNode:Node,
    )

    // endregion

    // region log lines

    private val unorderedFileLinesFlow:Flow<Map<File,List<FileLine>>> = concatenatedFilesFlow
        .distinctUntilChangedBy { files -> files.map { it.absolutePath }.toSet() }
        .map { files ->
            files
                .associateWith { file ->
                    println("Reading file: ${file.absolutePath}")
                    FileLines(
                        filePath = file.toFilePath(),
                        lines = file.readLines().withIndex().toList(),
                    )
                }
                .mapValues { mapEntry ->
                    val fileLines = mapEntry.value
                    fileLines.lines.map { line ->
                        val key = FileLineKey(
                            filePath = fileLines.filePath,
                            lineNumberInFile = line.index + 1,
                        )
                        FileLine(
                            key = key,
                            line = line.value,
                        )
                    }
                }
        }
        .flowOn(dispatchers.io)
        .shareIn(
            scope = uiScope.newChildScope(dispatchers.io),
            started = SharingStarted.WhileSubscribed(5.seconds),
            replay = 1,
        )

    private val orderedFileLines:Flow<List<FileLine>> = combine(concatenatedFilesFlow,unorderedFileLinesFlow)
    { concatenatedFiles,unorderedFileLines ->
        concatenatedFiles.flatMap { file -> unorderedFileLines[file] ?: emptyList() }
    }.flowOn(dispatchers.default).conflate()

    private data class FileLines(
        val filePath:FilePath,
        val lines:List<IndexedValue<String>>,
    )

    data class FileLine(
        val key:FileLineKey,
        val line:String,
    )

    data class FileLineKey(
        val filePath:FilePath,
        val lineNumberInFile:Int,
    )

    val logLinesFlow:Flow<LogLinesState> = combine(
        orderedFileLines,
        activeFilters,
        ::toLogLineState,
    ).flowOn(dispatchers.default).shareIn(
        scope = uiScope.newChildScope(dispatchers.io),
        started = SharingStarted.WhileSubscribed(5.seconds),
        replay = 1,
    )

    private fun toLogLineState(
        logLines:List<FileLine>,
        activeFilters:Map<FilterType, List<Node>>,
    ):LogLinesState
    {
        val filteredLines = applyFilterToLogLines(logLines, activeFilters)
        return LogLinesState(
            logLines = filteredLines,
            longestLineLength = filteredLines.maxOfOrNull { it.line.length } ?: 0,
        )
    }

    private fun applyFilterToLogLines(
        logLines:List<FileLine>,
        activeFilters:Map<FilterType, List<Node>>,
    ):List<FileLine> = logLines.filter { logLine ->

        // this is a reminder to make sure you added appropriate logic for each filter type in this code block
        FilterType.entries.forEach { filterType ->
            when (filterType)
            {
                FilterType.BOOKMARK,
                FilterType.INCLUDE,
                FilterType.EXCLUDE -> Unit
            }
        }

        val bookmarkFilters = activeFilters[FilterType.BOOKMARK].orEmpty()
        val includeFilters = activeFilters[FilterType.INCLUDE].orEmpty()
        val excludeFilters = activeFilters[FilterType.EXCLUDE].orEmpty()

        val isBookmarked = bookmarkFilters.any { filter -> logcatFilterEvaluator.isMatch(logLine.line,filter) }
        val shouldInclude = includeFilters.any { filter -> logcatFilterEvaluator.isMatch(logLine.line,filter) }
        val isNotExcluded = excludeFilters.all { filter -> logcatFilterEvaluator.isMatch(logLine.line,filter) }

        isBookmarked || ((includeFilters.isEmpty() || shouldInclude) && (excludeFilters.isEmpty() || isNotExcluded))
    }

    data class LogLinesState(

        /**
         * used by UI to display these lines on the UI.
         */
        val logLines:List<FileLine> = emptyList(),

        /**
         * used by UI to compute horizontal scrolling and max list item width.
         */
        val longestLineLength:Int = 0,
    )

    // endregion

    // region copy selected text to clipboard

    fun copySelectedTextToClipboard(
        logLines:List<FileLine>,
        selectedItems:SelectedItems<FileLineKey>,
        clipboardManager:Clipboard,
    )
    {
        uiScope.launch(dispatchers.io)
        {
            val textToCopy = logLines
                .filter { it.key in selectedItems }
                .joinToString("\n") { it.line }
            clipboardManager.setClipEntry(
                ClipEntry(StringSelection(textToCopy))
            )
        }
    }

    // endregion

    companion object
    {
        fun create(
            uiScope:ImmutableCoroutineScope,
            configurationId:ConfigurationId,
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
            logcatFilterParser:LogcatFilterParser = LogcatFilterParser(),
            logcatFilterEvaluator:LogcatFilterEvaluator = LogcatFilterEvaluatorImpl(),
        ):LogViewerViewModel = LogViewerViewModel(
            uiScope = uiScope,
            kotlinDependencyProvider = kotlinDependencyProvider,
            logcatFilterParser = logcatFilterParser,
            logcatFilterEvaluator = logcatFilterEvaluator,
            configurationId = configurationId,
        )
    }
}
