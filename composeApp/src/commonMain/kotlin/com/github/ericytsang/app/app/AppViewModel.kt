package com.github.ericytsang.app.app

import com.github.ericytsang.domain.objects.Theme
import com.github.ericytsang.domain.repo.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.ThemeRepository
import kotlinx.coroutines.flow.Flow

interface AppViewModel
{
    val theme:Flow<Theme>
    fun switchTheme()

    companion object
    {
        fun create(
            themeRepository:ThemeRepository = RepositoryDependencyProvider.instance.themeRepository,
        ):AppViewModel = AppViewModelImpl(
            themeRepository = themeRepository,
        )
    }
}

private class AppViewModelImpl(
    private val themeRepository:ThemeRepository,
):AppViewModel
{
    override val theme:Flow<Theme>
        get() = themeRepository.getThemeFlow()

    override fun switchTheme()
    {
        themeRepository.changeTheme()
    }
}
