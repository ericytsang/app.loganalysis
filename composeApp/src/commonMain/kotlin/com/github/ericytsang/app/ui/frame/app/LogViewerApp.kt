package com.github.ericytsang.app.ui.frame.app

import com.github.ericytsang.domain.objects.ConfigurationId
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking

interface LogViewerApp
{
    fun openNewProjectWizard()
    fun openProject(configurationId:ConfigurationId)
    fun openProjectBrowser()

    companion object
    {
        fun create(
            commandChannel:SendChannel<AppCommand>,
        ):LogViewerApp = LogViewerAppImpl(
            commandChannel = commandChannel,
        )
    }
}

private class LogViewerAppImpl(
    private val commandChannel:SendChannel<AppCommand>,
):LogViewerApp
{
    override fun openNewProjectWizard()
    {
        commandChannel.trySendBlocking(AppCommand.OpenNewProjectWizard)
    }

    override fun openProject(configurationId:ConfigurationId)
    {
        commandChannel.trySendBlocking(AppCommand.OpenProject(configurationId))
    }

    override fun openProjectBrowser()
    {
        commandChannel.trySendBlocking(AppCommand.OpenProjectBrowser)
    }
}

sealed class AppCommand
{
    data object OpenNewProjectWizard:AppCommand()
    data class OpenProject(val configurationId:ConfigurationId):AppCommand()
    data object OpenProjectBrowser:AppCommand()
}
