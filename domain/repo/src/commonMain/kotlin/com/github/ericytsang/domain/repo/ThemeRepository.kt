package com.github.ericytsang.domain.repo

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.github.ericytsang.domain.objects.Theme
import com.github.ericytsang.kotlin.KotlinDependencyProvider
import com.github.ericytsang.service.sqlite.dependencyinjection.SqliteDependencyProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.collections.get

interface ThemeRepository
{
    fun getTheme():Flow<Theme>
    fun changeTheme()
}

class ThemeRepositoryImpl(
    private val kotlinDependencyProvider:KotlinDependencyProvider,
    private val sqliteDependencyProvider:SqliteDependencyProvider,
    private val appInfoService:AppInfoService,
):ThemeRepository,KotlinDependencyProvider by kotlinDependencyProvider
{
    private val database by lazy { sqliteDependencyProvider.databaseFactory.getDatabase(appInfoService.getAppPackageName()) }

    override fun getTheme():Flow<Theme> = database.logAnalysisQueries.selectSettings().asFlow()
        .mapToList(dispatchers.io)
        .map { entities -> entities.firstOrNull() }
        .map { settingsEntity -> parseDbValueToTheme(settingsEntity?.theme) ?: Theme.LIGHT }
        .flowOn(dispatchers.io)

    override fun changeTheme()
    {
        applicationScope.launch(dispatchers.io)
        {
            database.transaction(noEnclosing = true)
            {
                // get the current theme
                val settings = database.logAnalysisQueries.selectSettings().executeAsOne()
                val theme = parseDbValueToTheme(settings.theme) ?: Theme.LIGHT

                // update the theme to change to the next one
                val newTheme = theme.getNextTheme()
                database.logAnalysisQueries.updateSettings(theme = newTheme.toDbValue())
            }
        }
    }

    companion object
    {
        // region mapping theme to db value

        private val themeByDbValue = Theme.entries.associateBy { it.toDbValue() }

        private fun parseDbValueToTheme(dbValue:String?):Theme?
        {
            return themeByDbValue[dbValue]
        }

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