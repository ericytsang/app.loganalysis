package com.github.ericytsang.app.app

import com.github.ericytsang.domain.repo.DelimiterService
import com.github.ericytsang.domain.repo.SettingsRepository
import com.github.ericytsang.domain.repo.SettingsRepositoryImpl
import kotlinx.coroutines.flow.Flow

interface AppViewModel:ThemeViewModel,DelimiterService
{
    companion object
    {
        fun create():AppViewModel = AppViewModelImpl()
    }
}

private class AppViewModelImpl(
    private val settingsRepository:SettingsRepository = SettingsRepositoryImpl(),
):
    AppViewModel,
    ThemeViewModel by ThemeViewModel.create()
{
    override fun getDelimiterFlow():Flow<String> = settingsRepository.getDelimiterFlow()
    override suspend fun setDelimiter(newDelimiters: String) = settingsRepository.setDelimiter(newDelimiters)
}

