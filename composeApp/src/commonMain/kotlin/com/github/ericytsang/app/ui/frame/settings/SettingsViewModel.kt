package com.github.ericytsang.app.ui.frame.settings

import com.github.ericytsang.app.ui.frame.settings.SettingsViewModel.DelimiterCharactersState
import com.github.ericytsang.app.usecase.MutableThemeUseCase
import com.github.ericytsang.app.util.LatestJobExecutor
import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.DelimiterRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface SettingsViewModel:MutableThemeUseCase
{
    val delimiterCharactersEnabled:Flow<Boolean>
    val delimiterCharactersText:Flow<String>
    fun setDelimiterCharacters(delimiterCharacters:String)

    sealed class DelimiterCharactersState
    {
        object LoadingInitialValueFromDb:DelimiterCharactersState()
        data class InMemoryValue(val delimiterCharacters:String):DelimiterCharactersState()
    }

    companion object
    {
        fun create(
            uiScope:CoroutineScope,
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.instance,
            delimiterRepository:DelimiterRepository = RepositoryDependencyProvider.instance.delimiterRepository,
        ):SettingsViewModel = SettingsViewModelImpl(
            uiScope = uiScope,
            kotlinDependencyProvider = kotlinDependencyProvider,
            delimiterRepository = delimiterRepository,
        )
    }
}

private class SettingsViewModelImpl(
    uiScope:CoroutineScope,
    private val kotlinDependencyProvider:KotlinDependencyProvider,
    private val delimiterRepository:DelimiterRepository,
):SettingsViewModel,
    MutableThemeUseCase by MutableThemeUseCase.create(),
    KotlinDependencyProvider by kotlinDependencyProvider
{
    private val delimiterCharactersFlow =
        MutableStateFlow<DelimiterCharactersState>(DelimiterCharactersState.LoadingInitialValueFromDb)

    init
    {
        uiScope.launch(dispatchers.io)
        {
            val valueFromDb = delimiterRepository.getDelimiterFlow().first()
            delimiterCharactersFlow.value = DelimiterCharactersState.InMemoryValue(valueFromDb)
        }
    }

    override val delimiterCharactersEnabled:Flow<Boolean> = delimiterCharactersFlow.map { delimiterCharactersState ->
        when (delimiterCharactersState)
        {
            is DelimiterCharactersState.LoadingInitialValueFromDb -> false
            is DelimiterCharactersState.InMemoryValue -> true
        }
    }

    override val delimiterCharactersText:Flow<String> = delimiterCharactersFlow.map { delimiterCharactersState ->
        when (delimiterCharactersState)
        {
            is DelimiterCharactersState.LoadingInitialValueFromDb -> ""
            is DelimiterCharactersState.InMemoryValue -> delimiterCharactersState.delimiterCharacters
        }
    }

    override fun setDelimiterCharacters(delimiterCharacters:String)
    {
        delimiterCharactersFlow.value = DelimiterCharactersState.InMemoryValue(delimiterCharacters)
        executor.submit()
        {
            delimiterRepository.setDelimiter(delimiterCharacters)
        }
    }

    companion object
    {
        private val executor = LatestJobExecutor(
            scope = KotlinDependencyProvider.instance.applicationScope,
            coroutineDispatcher = KotlinDependencyProvider.instance.dispatchers.io,
        )
    }
}
