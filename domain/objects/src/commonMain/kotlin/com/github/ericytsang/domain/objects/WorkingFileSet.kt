package com.github.ericytsang.domain.objects

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

data class LogFile(
    val filePath: String,
    val orderIndex: OrderIndex,
)
{
    val file: File get() = File(filePath)
}

data class ConfigurationId(
    val id: Long,
)

data class OrderIndex(
    val orderIndex: Long,
)

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
        val min = ConfigurationUpdateSequence(Long.MIN_VALUE)
    }
}

data class FilterModel(
    val id:FilterModelId,
    val configurationId:ConfigurationId,
    val filterString:String,
    val isCaseSensitive:Boolean,
    val filterInterpretationMode:FilterInterpretationMode,
    val filterType:FilterType,
    val isActive:Boolean,
    val orderIndex:OrderIndex,
)

data class FilterModelId(
    val id:Long,
)

enum class FilterType
{
    INCLUDE,
    EXCLUDE,
}

enum class FilterInterpretationMode
{
    STRING_LITERAL,
    REGULAR_EXPRESSION,
    LOGCAT_FILTER,
}
