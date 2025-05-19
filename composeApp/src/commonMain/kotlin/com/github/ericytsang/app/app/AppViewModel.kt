package com.github.ericytsang.app.app

import com.github.ericytsang.domain.objects.Theme
import com.github.ericytsang.domain.repo.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.ThemeRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.kotlin.KotlinDependencyProviderImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface AppViewModel
{
    val theme:Flow<Theme>
    fun switchTheme()

    companion object
    {
        fun create(
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProviderImpl,
            themeRepository:ThemeRepository = RepositoryDependencyProvider.instance.themeRepository,
        ):AppViewModel = AppViewModelImpl(
            kotlinDependencyProvider = kotlinDependencyProvider,
            themeRepository = themeRepository,
        )
    }
}

private class AppViewModelImpl(
    private val kotlinDependencyProvider:KotlinDependencyProvider,
    private val themeRepository:ThemeRepository,
):AppViewModel,KotlinDependencyProvider by kotlinDependencyProvider
{
    override val theme:Flow<Theme>
        get() = themeRepository.getThemeFlow()

    override fun switchTheme()
    {
        applicationScope.launch()
        {
            themeRepository.changeTheme()
        }
    }
}
