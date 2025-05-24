package com.github.ericytsang.app.app

import com.github.ericytsang.app.util.NowFactory
import com.github.ericytsang.app.util.TwoWayStringBindingUseCase
import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.DelimiterRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface SettingsViewModel:MutableThemeUseCase
{
    val delimiterCharacters:Flow<String>
    fun setDelimiterCharacters(delimiterCharacters:String)

    companion object
    {
        fun create(
            uiScope:CoroutineScope,
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
            delimiterRepository:DelimiterRepository = RepositoryDependencyProvider.Companion.instance.delimiterRepository,
            nowFactory: NowFactory = NowFactory.instance,
        ):SettingsViewModel = SettingsViewModelImpl(
            uiScope = uiScope,
            kotlinDependencyProvider = kotlinDependencyProvider,
            delimiterRepository = delimiterRepository,
            nowFactory = nowFactory,
        )
    }
}

private class SettingsViewModelImpl(
    uiScope:CoroutineScope,
    private val kotlinDependencyProvider:KotlinDependencyProvider,
    private val delimiterRepository:DelimiterRepository,
    private val nowFactory:NowFactory,
):SettingsViewModel,
    MutableThemeUseCase by MutableThemeUseCase.create(),
    KotlinDependencyProvider by kotlinDependencyProvider
{
    private val delimiterCharactersUseCase = TwoWayStringBindingUseCase(
        uiScope = uiScope,
        remoteStringFlow = delimiterRepository.getDelimiterFlow(),
        updateRemoteString = { newString -> delimiterRepository.setDelimiter(newString) },
        kotlinDependencyProvider = kotlinDependencyProvider,
        nowFactory = nowFactory,
    )

    override val delimiterCharacters:Flow<String> get() = delimiterCharactersUseCase.displayString

    override fun setDelimiterCharacters(delimiterCharacters:String)
    {
        delimiterCharactersUseCase.updateString(delimiterCharacters)
    }
}
