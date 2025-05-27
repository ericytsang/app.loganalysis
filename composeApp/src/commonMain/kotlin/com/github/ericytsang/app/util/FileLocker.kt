package com.github.ericytsang.app.util

import com.github.ericytsang.domain.appinfo.AppInfoService
import java.io.File
import java.time.Instant
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
            showFatalErrorDialog(
                """App is already running.
If it is not running, please delete the lock file and try again:
${systemWideAppLockFile.absolutePath}"""
            )
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
        if (isThereAnOngoingProcess(file)) return false

        // try to lock the file
        createIfNotExists(file)
        val myProcessInfo = getMyProcessInfo()
        file.writeText("${myProcessInfo.pid}\n${myProcessInfo.startTime}")

        // double-check that the file is now locked by this process in case of race conditions
        Thread.sleep(500)
        return isTheLockFileStillLockedByUs(file)
    }

    private fun createIfNotExists(file:File)
    {
        if (!file.exists())
        {
            file.parentFile?.mkdirs()
            file.createNewFile()
        }
    }

    private fun isThereAnOngoingProcess(file:File):Boolean
    {
        return getProcessInfoFromLockFile(file).fold(

            // check if the process info in the lock file matches any currently living process
            onSuccess = { it != null && isProcessAlive(it) },

            // assume that there is an ongoing process if the lock file is malformed
            onFailure = { true },
        )
    }

    private fun isTheLockFileStillLockedByUs(file:File):Boolean
    {
        return getProcessInfoFromLockFile(file).fold(

            // check if the process info in the lock file matches this process
            onSuccess = { it != null && it == getMyProcessInfo() },

            // assume that the lock file is not locked by us if it is malformed
            onFailure = { false },
        )
    }

    private fun isProcessAlive(processInfo:ProcessInfo):Boolean
    {
        return try
        {

            // Attempt to open the process with the given ID
            ProcessHandle.of(processInfo.pid)
                .map { it.isAlive && it.toProcessInfo() == processInfo }
                .orElse(false)
        }
        catch (_:Exception)
        {
            // If an exception occurs, the process is likely not alive
            false
        }
    }

    private fun getMyProcessInfo():ProcessInfo = ProcessHandle.current().toProcessInfo()

    private fun getProcessInfoFromLockFile(file:File):Result<ProcessInfo?>
    {
        return runCatching()
        {
            // there is no lock file, then there is no process info
            if (!file.exists()) return@runCatching null

            // read the lock file and parse the process info
            file.reader().useLines { lines ->
                val lineList = lines.take(2).toList().takeIf { it.size == 2 } ?: error("Lock file is malformed")
                ProcessInfo(
                    pid = lineList[0].toLong(),
                    startTime = Instant.parse(lineList[1]),
                )
            }
        }
    }

    private fun ProcessHandle.toProcessInfo():ProcessInfo
    {
        return ProcessInfo(
            pid = pid(),
            startTime = info().startInstant().orElse(Instant.ofEpochSecond(0)),
        )
    }

    private data class ProcessInfo(
        val pid:Long,
        val startTime:Instant
    )
}
