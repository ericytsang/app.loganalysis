package com.github.ericytsang.domain.repo

import com.github.ericytsang.domain.objects.WorkingFileSet
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.flow.Flow

class LogLineRepoImpl(
    private val kotlinDependencyProvider:KotlinDependencyProvider,
    private val workingFileSetFlow:Flow<WorkingFileSet>,
):LogLineRepo,KotlinDependencyProvider by kotlinDependencyProvider
{
    private val logLines = mutableListOf<String>()

    init
    {
        workingFileSet.collect
    }

    override fun getLogLine(index:Int):String
    {
        return logLines[index]
    }
}

interface LogLineRepo
{
    fun getLogLine(index:Int):String
}

interface DomainDependencyProvider
{
    val kotlinDependencyProvider:KotlinDependencyProvider
    val logLineRepo:LogLineRepo
}
