package com.github.ericytsang.domain.repo.repo

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.github.ericytsang.domain.objects.Theme
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.service.sqlite.dbfactory.DatabaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.collections.get

interface ThemeRepository
{
    fun getThemeFlow():Flow<Theme>
    suspend fun changeTheme()
}

internal class ThemeRepositoryImpl(
    private val kotlinDependencyProvider:KotlinDependencyProvider,
    private val databaseService:DatabaseService,
):ThemeRepository,
    KotlinDependencyProvider by kotlinDependencyProvider
{
    override fun getThemeFlow():Flow<Theme> = databaseService.queries.selectSettings().asFlow()
        .mapToList(dispatchers.io)
        .map { entities -> entities.firstOrNull() }
        .map { settingsEntity -> parseDbValueToTheme(settingsEntity?.theme) ?: Theme.LIGHT }
        .flowOn(dispatchers.io)

    override suspend fun changeTheme() = withContext<Unit>(dispatchers.io)
    {
        this@ThemeRepositoryImpl.databaseService.transaction()
        {
            // get the current theme
            val settings = this@ThemeRepositoryImpl.databaseService.queries.selectSettings().executeAsOne()
            val theme = parseDbValueToTheme(settings.theme) ?: Theme.LIGHT

            // update the theme to change to the next one
            val newTheme = theme.getNextTheme()
            this@ThemeRepositoryImpl.databaseService.queries.updateTheme(theme = newTheme.toDbValue())
        }
    }

    companion object
    {
        // region mapping theme to db value

        private val themeByDbValue = Theme.entries.associateBy { it.toDbValue() }

        private fun parseDbValueToTheme(dbValue:String?):Theme? = themeByDbValue[dbValue]

        private fun Theme.toDbValue():String = when (this)
        {
            Theme.LIGHT -> "light"
            Theme.DARK -> "dark"
        }

        // endregion

        private fun Theme.getNextTheme() = when (this)
        {
            Theme.LIGHT -> Theme.DARK
            Theme.DARK -> Theme.LIGHT
        }
    }
}
