package com.github.ericytsang.service.sqlite

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.util.Properties
import app.cash.sqldelight.db.SqlDriver
import java.io.File

/**
 * This is the implementation of the DatabaseDriverFactory for JVM.
 * It creates a SQLite database driver using the JDBC driver.
 * The database file is stored in the user's home directory.
 */
actual class DatabaseDriverFactory
{
    actual fun createDriver(appPackageName:String):SqlDriver
    {
        // need to create the directory if it doesn't exist, otherwise the database file won't be created
        val appHomePath = getAndCreateAppHome(appPackageName)

        // use the user home directory to store the database file
        val appDatabasePath = File(appHomePath,DB_FILE_NAME)
        val appDatabaseFileUri = appDatabasePath.toURI().toASCIIString()

        // create the database driver
        return JdbcSqliteDriver(
            url = "$SQLITE_JDBC_CONNECTIONS_SCHEME$appDatabaseFileUri",
            properties = Properties(),
            schema = SqlDelightDatabase.Schema,
        )
    }

    private fun getAndCreateAppHome(appPackageName:String):File
    {
        val userHome = getUserHome()
        val appHome = File(userHome,".$appPackageName")
        appHome.mkdirs()
        return appHome
    }

    private fun getUserHome():File
    {
        val userHomePath = System.getProperty("user.home")
        return File(userHomePath)
    }

    companion object
    {
        private const val DB_FILE_NAME = "app-data.sqldelight.db"
        private const val SQLITE_JDBC_CONNECTIONS_SCHEME = "jdbc:sqlite:"
    }
}
