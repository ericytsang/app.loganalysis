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
    fun tryLock(lockFile:File):Boolean
    {
        val myPid = ProcessHandle.current().pid()

        // create a new file with our PID in the file name, and in the first line of the file
        val tempLockFile = lockFile.resolveSibling("${lockFile.name}.$myPid.tmp")
        createIfNotExists(tempLockFile)
        tempLockFile.writeText(myPid.toString())

        // see if the file is already locked by another process
        val lockingPid = if (lockFile.exists())
        {
            lockFile.reader().useLines { lines -> lines.firstOrNull()?.toLong() }
        }
        else
        {
            null
        }
        if (lockingPid != null && isProcessAlive(lockingPid))
        {
            // file is already locked by another process
            return false
        }

        // try to lock the file; failure to lock the file, means process has beaten us to it
        if (!tempLockFile.renameTo(lockFile))
        {
            tempLockFile.delete()
            return false
        }

        return true
    }

    private fun createIfNotExists(file:File)
    {
        if (!file.exists())
        {
            file.parentFile?.mkdirs()
            file.createNewFile()
        }
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

