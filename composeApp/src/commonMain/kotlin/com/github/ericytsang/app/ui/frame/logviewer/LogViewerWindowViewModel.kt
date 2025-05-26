package com.github.ericytsang.app.ui.frame.logviewer

import com.github.ericytsang.app.usecase.ThemeUseCase
import com.github.ericytsang.domain.repo.dependencyinjection.RepositoryDependencyProvider
import com.github.ericytsang.domain.repo.repo.DelimiterRepository
import com.github.ericytsang.domain.repo.repo.SettingsRepository
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

interface LogViewerWindowViewModel:ThemeUseCase,DelimiterRepository
{
    fun getWordWrapFlow():Flow<Boolean>

    fun toggleWordWrap()

    companion object
    {
        fun create(
            uiScope:CoroutineScope,
            kotlinDependencyProvider:KotlinDependencyProvider = KotlinDependencyProvider.Companion.instance,
            settingsRepository:SettingsRepository = RepositoryDependencyProvider.Companion.instance.settingsRepository,
            delimiterRepository:DelimiterRepository = RepositoryDependencyProvider.Companion.instance.delimiterRepository,
        ):LogViewerWindowViewModel = LogViewerComposableViewModelImpl(
            uiScope = uiScope,
            kotlinDependencyProvider = kotlinDependencyProvider,
            settingsRepository = settingsRepository,
            delimiterRepository = delimiterRepository,
        )
    }
}

private class LogViewerComposableViewModelImpl(
    uiScope:CoroutineScope,
    private val kotlinDependencyProvider:KotlinDependencyProvider,
    private val settingsRepository:SettingsRepository,
    private val delimiterRepository:DelimiterRepository,
):
    LogViewerWindowViewModel,
    ThemeUseCase by ThemeUseCase.Companion.create(),
    KotlinDependencyProvider by kotlinDependencyProvider,
    DelimiterRepository by delimiterRepository
{
    // region should wrap long text setting

    /**
     * the UX for this is to:
     * - have a toggle button on the log viewer window
     * - the setting is not synced with other active instances of the app
     * - the setting is only applied to the current instance, and future new instances of the app
     */
    private val _shouldWrapLongTextFlow = MutableStateFlow<Boolean?>(null)

    init
    {
        uiScope.launch(dispatchers.io)
        {
            _shouldWrapLongTextFlow.value = settingsRepository.getShouldWrapTextFlow().first()
        }
    }

    override fun getWordWrapFlow():Flow<Boolean> = _shouldWrapLongTextFlow.filterNotNull()

    override fun toggleWordWrap()
    {
        val oldValue = _shouldWrapLongTextFlow.value ?: return
        val newValue = !oldValue
        _shouldWrapLongTextFlow.value = newValue
        applicationScope.launch(dispatchers.io)
        {
            settingsRepository.setShouldWrapText(newValue)
        }
    }

    // endregion
}