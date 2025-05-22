package com.github.ericytsang.app.app

import com.github.ericytsang.domain.objects.Theme
import com.github.ericytsang.domain.repo.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.SettingsRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.kotlin.KotlinDependencyProviderImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface ThemeViewModel
{
    val theme:Flow<Theme>

    companion object
    {
        fun create(
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProviderImpl,
            themeRepository:SettingsRepository = RepositoryDependencyProvider.Companion.instance.settingsRepository,
        ):ThemeViewModel = MutableThemeViewModelImpl(
            kotlinDependencyProvider = kotlinDependencyProvider,
            settingsRepository = themeRepository,
        )
    }
}

interface MutableThemeViewModel:ThemeViewModel
{
    fun switchTheme()

    companion object
    {
        fun create(
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProviderImpl,
            themeRepository:SettingsRepository = RepositoryDependencyProvider.Companion.instance.settingsRepository,
        ):MutableThemeViewModel = MutableThemeViewModelImpl(
            kotlinDependencyProvider = kotlinDependencyProvider,
            settingsRepository = themeRepository,
        )
    }
}

private class MutableThemeViewModelImpl(
    private val kotlinDependencyProvider:KotlinDependencyProvider,
    private val settingsRepository:SettingsRepository,
):MutableThemeViewModel,KotlinDependencyProvider by kotlinDependencyProvider
{
    override val theme:Flow<Theme>
        get() = settingsRepository.getThemeFlow()

    override fun switchTheme()
    {
        applicationScope.launch()
        {
            settingsRepository.changeTheme()
        }
    }
}