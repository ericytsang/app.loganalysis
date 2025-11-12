package com.github.ericytsang.domain.objects

import com.github.ericytsang.domain.objects.FilePath.Companion.toFile
import java.io.File

sealed class WorkingFileSet
{
    abstract val configurationId: ConfigurationId?
    abstract val files: List<LogFile>
}

data class WorkingFileSetSelected(
    override val configurationId: ConfigurationId,
    override val files: List<LogFile>,
):WorkingFileSet()

data object WorkingFileSetEmpty:WorkingFileSet()
{
    override val configurationId: ConfigurationId? get() = null
    override val files: List<LogFile> get() = emptyList()
}

data class FilePath(
    val filePath: String,
)
{
    companion object
    {
        fun File.toFilePath(): FilePath = FilePath(filePath = absolutePath)
        fun FilePath.toFile(): File = File(filePath)
    }
}

data class LogFile(
    val filePath: FilePath,
    val orderIndex: OrderIndex,
)
{
    val file: File get() = filePath.toFile()
}

data class ConfigurationId(
    val id: Long,
)

data class OrderIndex(
    val orderIndex: Long,
):Comparable<OrderIndex>
{
    override fun compareTo(other: OrderIndex): Int = orderIndex.compareTo(other.orderIndex)
}

data class Configuration(
    val id: ConfigurationId,
    val name: ConfigurationName,
    val updateSequence: ConfigurationUpdateSequence,
    val logcatFilter: String,
)

data class ConfigurationName(
    val name: String,
)

data class ConfigurationUpdateSequence(
    val updateSequence: Long,
)
{
    companion object
    {
        val max = ConfigurationUpdateSequence(Long.MAX_VALUE)
    }
}

data class FilterModel(
    val id:FilterId,
    val configurationId:ConfigurationId,
    val filterString:String,
    val isCaseSensitive:Boolean,
    val filterInterpretationMode:FilterInterpretationMode,
    val filterType:FilterType,
    val isActive:Boolean,
    val orderIndex:OrderIndex,
)

data class FilterId(
    val id:Long,
)

enum class FilterType
{
    INCLUDE,
    EXCLUDE,
    BOOKMARK,
}

enum class FilterInterpretationMode
{
    STRING_LITERAL,
    REGULAR_EXPRESSION,
    LOGCAT_FILTER,
}
