package com.github.ericytsang.app.ui.frame.app

import com.github.ericytsang.domain.objects.ConfigurationUpdateSequence
import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.ConfigurationRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.withContext

interface FirstAppActionComputer
{
    suspend fun decideFirstAppAction():FirstAppAction
    suspend fun perform(appCommandHandler:AppCommandHandler,firstAppAction:FirstAppAction)

    companion object
    {
        fun create():FirstAppActionComputer = FirstAppActionComputerImpl()
    }
}

sealed class FirstAppAction
{
    /**
     * Opens the "New Project" wizard.
     * This is the default action when the app is launched, and there are no projects at all.
     * It will show a UI allowing the user to choose what log files to include in the project,
     * and letting them re-order the log files, too.
     */
    data object OpenNewProjectWizard:FirstAppAction()

    /**
     * Opens all previously opened projects.
     * If upon the last app close, there were still projects that were opened, then this action will be taken
     * to restore the state of the app to what it was before the last close.
     */
    data object OpenPreviouslyOpenedProjects:FirstAppAction()

    /**
     * If there were some projects, but none of them were opened upon the last app close,
     * then this action will be taken to open the project browser, allowing the user to
     * pick a previous project to load, or to open the "New Project" wizard,
     */
    data object OpenProjectBrowser:FirstAppAction()
}

private class FirstAppActionComputerImpl(
    kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.Companion.instance,
    private val configurationRepository:ConfigurationRepository = RepositoryDependencyProvider.Companion.instance.configurationRepository,
):FirstAppActionComputer,
    KotlinDependencyProvider by kotlinDependencyProvider
{
    override suspend fun decideFirstAppAction():FirstAppAction = withContext(dispatchers.io)
    {
        // if there are no projects at all, then open the "New Project" wizard
        val oldProjects =
            configurationRepository.loadNextNConfigurationIdsBefore(1,ConfigurationUpdateSequence.Companion.max)
        if (oldProjects.isEmpty())
        {
            return@withContext FirstAppAction.OpenNewProjectWizard
        }

        // if there are projects that are active, then open them up
        val activeProjects = configurationRepository.getActiveProjects()
        if (activeProjects.hasNext())
        {
            return@withContext FirstAppAction.OpenPreviouslyOpenedProjects
        }

        // if there are projects, but none of them were active, then open the project browser
        return@withContext FirstAppAction.OpenProjectBrowser
    }

    override suspend fun perform(appCommandHandler:AppCommandHandler, firstAppAction:FirstAppAction)
    {
        when (firstAppAction)
        {
            is FirstAppAction.OpenNewProjectWizard -> appCommandHandler.openNewProjectWizard()
            is FirstAppAction.OpenPreviouslyOpenedProjects ->
            {
                val activeProjects = configurationRepository.getActiveProjects()
                while (activeProjects.hasNext())
                {
                    appCommandHandler.openProject(activeProjects.next().id)
                }
            }
            is FirstAppAction.OpenProjectBrowser -> appCommandHandler.openProjectBrowser()
        }
    }
}