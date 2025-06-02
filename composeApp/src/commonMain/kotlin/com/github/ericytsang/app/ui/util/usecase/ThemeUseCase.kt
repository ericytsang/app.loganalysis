package com.github.ericytsang.app.ui.util.usecase

import com.github.ericytsang.domain.objects.Theme
import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.ThemeRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.flow.Flow

interface ThemeUseCase
{
    val theme:Flow<Theme>

    companion object
    {
        val instance:ThemeUseCase get() = mutableInstance

        val mutableInstance:MutableThemeUseCase by lazy {
            MutableThemeUseCaseImpl(
                kotlinDependencyProvider = KotlinDependencyProvider.instance,
                themeRepository = RepositoryDependencyProvider.instance.themeRepository,
            )
        }
    }
}

interface MutableThemeUseCase:ThemeUseCase
{
    fun switchTheme()

    companion object
    {
        fun create(
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
            themeRepository:ThemeRepository = RepositoryDependencyProvider.instance.themeRepository,
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