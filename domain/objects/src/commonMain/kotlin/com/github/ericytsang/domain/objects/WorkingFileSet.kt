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
