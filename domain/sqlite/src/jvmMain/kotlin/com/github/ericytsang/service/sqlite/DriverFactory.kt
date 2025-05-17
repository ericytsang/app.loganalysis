package com.github.ericytsang.service.sqlite

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.util.Properties
import app.cash.sqldelight.db.SqlDriver
import java.io.File

actual class DriverFactory
{
    actual fun createDriver(appPackageName:String):SqlDriver
    {
        // need to create the directory if it doesn't exist, otherwise the database file won't be created
        val appHomePath = getAndCreateAppHome(appPackageName)

        // use the user home directory to store the database file
        val appDatabasePath = File(appHomePath,"sql.db")

        // create the database driver
        return JdbcSqliteDriver(
            url = "jdbc:sqlite:${appDatabasePath.toURI().toASCIIString()}",
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
}
