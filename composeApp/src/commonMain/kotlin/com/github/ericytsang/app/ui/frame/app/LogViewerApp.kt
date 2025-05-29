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
            openNewProjectWizardChannel:SendChannel<Unit>,
            openProjectChannel:SendChannel<ConfigurationId>,
            openProjectBrowserChannel:SendChannel<Unit>,
        ):LogViewerApp = LogViewerAppImpl(
            openNewProjectWizardChannel = openNewProjectWizardChannel,
            openProjectChannel = openProjectChannel,
            openProjectBrowserChannel = openProjectBrowserChannel,
        )
    }
}

private class LogViewerAppImpl(
    private val openNewProjectWizardChannel:SendChannel<Unit>,
    private val openProjectChannel:SendChannel<ConfigurationId>,
    private val openProjectBrowserChannel:SendChannel<Unit>,
):LogViewerApp
{
    override fun openNewProjectWizard()
    {
        openNewProjectWizardChannel.trySendBlocking(Unit)
    }

    override fun openProject(configurationId:ConfigurationId)
    {
        openProjectChannel.trySendBlocking(configurationId)
    }

    override fun openProjectBrowser()
    {
        openProjectBrowserChannel.trySendBlocking(Unit)
    }
}