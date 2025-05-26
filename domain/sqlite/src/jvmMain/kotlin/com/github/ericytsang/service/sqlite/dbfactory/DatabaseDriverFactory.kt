package com.github.ericytsang.service.sqlite.dbfactory

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.github.ericytsang.service.sqlite.SqlDelightDatabase
import java.io.File
import java.util.Properties

/**
 * This is the implementation of the DatabaseDriverFactory for JVM.
 * It creates a SQLite database driver using the JDBC driver.
 * The database file is stored in the user's home directory.
 */
internal actual class DatabaseDriverFactory
{
    actual fun createDriver(appPackageName:String):SqlDriver
    {
        // need to create the directory if it doesn't exist, otherwise the database file won't be created
        val appHomePath = getAndCreateAppHome(appPackageName)

        // use the user home directory to store the database file
        val appDatabasePath = File(appHomePath,DB_FILE_NAME)
        val appDatabaseFileUri = appDatabasePath.toURI().toASCIIString()

        // create the database driver
        val sqliteDriver = JdbcSqliteDriver(
            url = "$SQLITE_JDBC_CONNECTIONS_SCHEME$appDatabaseFileUri",
            properties = Properties(),
            schema = SqlDelightDatabase.Schema,
        )

        return sqliteDriver
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