package com.github.ericytsang.app.app

import com.github.ericytsang.domain.objects.WorkingFileSet
import java.io.File

interface WorkingFileSetEditorViewModel
{
    val workingFileSet:WorkingFileSet
    fun addFiles(newFiles:List<File>)
    fun moveFilesToPosition(files:List<File>,position:Int)
    fun removeFiles(files:List<File>)

    companion object
    {
        fun createDefault():WorkingFileSetEditorViewModel = getWorkingFileSetEditorViewModelNullImpl()
    }
}

private fun getWorkingFileSetEditorViewModelNullImpl(
):WorkingFileSetEditorViewModel = object:WorkingFileSetEditorViewModel
{
    override val workingFileSet:WorkingFileSet get() = WorkingFileSet(files = emptyList())
    override fun addFiles(newFiles:List<File>) = Unit
    override fun moveFilesToPosition(files:List<File>,position:Int) = Unit
    override fun removeFiles(files:List<File>) = Unit
}