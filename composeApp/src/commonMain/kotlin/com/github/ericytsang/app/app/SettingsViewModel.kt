package com.github.ericytsang.app.app

import com.github.ericytsang.domain.repo.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.SettingsRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.kotlin.KotlinDependencyProviderImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface SettingsViewModel:MutableThemeViewModel
{
    val delimiterCharacters:Flow<String>
    fun setDelimiterCharacters(delimiterCharacters:String)

    companion object
    {
        fun create(
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProviderImpl,
            settingsRepository:SettingsRepository = RepositoryDependencyProvider.Companion.instance.settingsRepository,
        ):SettingsViewModel = SettingsViewModelImpl(
            kotlinDependencyProvider = kotlinDependencyProvider,
            settingsRepository = settingsRepository,
        )
    }
}

private class SettingsViewModelImpl(
    private val kotlinDependencyProvider:KotlinDependencyProvider,
    private val settingsRepository:SettingsRepository,
):SettingsViewModel,
    MutableThemeViewModel by MutableThemeViewModel.create(),
    KotlinDependencyProvider by kotlinDependencyProvider
{
    override val delimiterCharacters:Flow<String> get() = settingsRepository.getDelimiterFlow()

    override fun setDelimiterCharacters(delimiterCharacters:String)
    {
        applicationScope.launch(dispatchers.io)
        {
            settingsRepository.setDelimiter(delimiterCharacters)
        }
    }
}
