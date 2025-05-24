package com.github.ericytsang.app.usecase

import com.github.ericytsang.domain.objects.Theme
import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.ThemeRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface ThemeUseCase
{
    val theme:Flow<Theme>

    companion object
    {
        fun create(
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
            themeRepository:ThemeRepository = RepositoryDependencyProvider.Companion.instance.themeRepository,
        ):ThemeUseCase = MutableThemeUseCaseImpl(
            kotlinDependencyProvider = kotlinDependencyProvider,
            themeRepository = themeRepository,
        )
    }
}

interface MutableThemeUseCase:ThemeUseCase
{
    fun switchTheme()

    companion object
    {
        fun create(
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
            themeRepository:ThemeRepository = RepositoryDependencyProvider.Companion.instance.themeRepository,
        ):MutableThemeUseCase = MutableThemeUseCaseImpl(
            kotlinDependencyProvider = kotlinDependencyProvider,
            themeRepository = themeRepository,
        )
    }
}

private class MutableThemeUseCaseImpl(
    private val kotlinDependencyProvider:KotlinDependencyProvider,
    private val themeRepository:ThemeRepository,
):MutableThemeUseCase,KotlinDependencyProvider by kotlinDependencyProvider
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