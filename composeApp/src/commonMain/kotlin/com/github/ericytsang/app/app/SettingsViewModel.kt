package com.github.ericytsang.app.app

import com.github.ericytsang.app.util.NowFactory
import com.github.ericytsang.app.util.NowFactoryImpl
import com.github.ericytsang.app.util.TwoWayStringBindingUseCase
import com.github.ericytsang.domain.repo.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.SettingsRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.kotlin.KotlinDependencyProviderImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface SettingsViewModel:MutableThemeViewModel
{
    val delimiterCharacters:Flow<String>
    fun setDelimiterCharacters(delimiterCharacters:String)

    companion object
    {
        fun create(
            uiScope:CoroutineScope,
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProviderImpl,
            settingsRepository:SettingsRepository = RepositoryDependencyProvider.Companion.instance.settingsRepository,
        ):SettingsViewModel = SettingsViewModelImpl(
            uiScope = uiScope,
            kotlinDependencyProvider = kotlinDependencyProvider,
            settingsRepository = settingsRepository,
        )
    }
}

private class SettingsViewModelImpl(
    uiScope:CoroutineScope,
    private val kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProviderImpl,
    private val settingsRepository:SettingsRepository = RepositoryDependencyProvider.Companion.instance.settingsRepository,
    private val nowFactory:NowFactory = NowFactoryImpl,
):SettingsViewModel,
    MutableThemeViewModel by MutableThemeViewModel.create(),
    KotlinDependencyProvider by kotlinDependencyProvider
{
    private val delimiterCharactersUseCase = TwoWayStringBindingUseCase(
        uiScope = uiScope,
        remoteStringFlow = settingsRepository.getDelimiterFlow(),
        updateRemoteString = { newString -> settingsRepository.setDelimiter(newString) },
        kotlinDependencyProvider = kotlinDependencyProvider,
        nowFactory = nowFactory,
    )

    override val delimiterCharacters:Flow<String> get() = delimiterCharactersUseCase.displayString

    override fun setDelimiterCharacters(delimiterCharacters:String)
    {
        delimiterCharactersUseCase.updateString(delimiterCharacters)
    }
}
