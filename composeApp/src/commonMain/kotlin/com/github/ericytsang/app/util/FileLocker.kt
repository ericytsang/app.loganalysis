package com.github.ericytsang.app.util

import com.github.ericytsang.domain.appinfo.AppInfoService
import java.io.File
import javax.swing.JOptionPane
import kotlin.system.exitProcess

class EnsureSingletonProcessInstance(
    private val fileLocker:FileLocker = FileLocker(),
    private val appInfoService:AppInfoService = AppInfoService.instance,
)
{
    fun tryLock()
    {
        val appHome = appInfoService.getAndCreateAppHome()
        val systemWideAppLockFile = File(appHome, "singleton-process.lock")
        if (!fileLocker.tryLock(systemWideAppLockFile))
        {
            showFatalErrorDialog("App is already running.")
        }
    }

    private fun showFatalErrorDialog(message:String)
    {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE)
        exitProcess(0)
    }
}

class FileLocker()
{
    /**
     * Attempts to create the file and lock it for the duration of the process.
     * Returns true if the lock was acquired, false otherwise.
     */
    fun tryLock(file:File):Boolean
    {
        // see if the file is already locked by another live process
        if (isFileLockedByAnotherLiveProcess(file)) return false

        // try to lock the file
        createIfNotExists(file)
        val myPid = ProcessHandle.current().pid()
        file.writeText(myPid.toString())

        // double-check that the file is now locked by this process in case of race conditions
        Thread.sleep(500)
        return !isFileLockedByAnotherLiveProcess(file)
    }

    private fun createIfNotExists(file:File)
    {
        if (!file.exists())
        {
            file.parentFile?.mkdirs()
            file.createNewFile()
        }
    }

    private fun isFileLockedByAnotherLiveProcess(file:File):Boolean
    {
        // if file does not exist, then it cannot be locked
        if (!file.exists()) return false

        // see if the file is already locked by another process
        val lockingPid = file.reader().useLines { lines -> lines.firstOrNull()?.toLong() }
        return lockingPid != null && isProcessAlive(lockingPid)
    }

    private fun isProcessAlive(processId:Long):Boolean
    {
        return try
        {
            // Attempt to open the process with the given ID
            ProcessHandle.of(processId).map { it.isAlive }.orElse(false)
        }
        catch (_:Exception)
        {
            // If an exception occurs, the process is likely not alive
            false
        }
    }
}

