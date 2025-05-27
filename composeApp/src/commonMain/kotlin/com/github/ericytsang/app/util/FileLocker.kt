package com.github.ericytsang.app.util

import com.github.ericytsang.domain.appinfo.AppInfoService
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.OverlappingFileLockException
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
            showFatalErrorDialog("This app is already running.")
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
    fun tryLock(fileObj:File):Boolean
    {
        fileObj.parentFile?.mkdirs()
        fileObj.createNewFile()
        val raf = RandomAccessFile(fileObj,"rw")
        val channel = raf.channel
        val lock = try
        {
            channel.lock()
        }
        catch (_:OverlappingFileLockException)
        {
            // it is OK to have an overlapping lock in this case,
            // because we are only using this to ensure that 1 process
            // is using the file at a time, and we are not trying to
            // modify the file while it is locked.
            null
        }
        return if (lock != null)
        {
            true
        }
        else
        {
            raf.close()
            false
        }
    }
}

