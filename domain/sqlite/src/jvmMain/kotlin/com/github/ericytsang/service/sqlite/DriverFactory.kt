package com.github.ericytsang.service.sqlite

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.util.Properties
import app.cash.sqldelight.db.SqlDriver
import java.io.File

actual class DriverFactory
{
    actual fun createDriver(appPackageName:String):SqlDriver
    {
        // use the user home directory to store the database file
        val userHomePath = System.getProperty("user.home")
        val appHomePath = File(userHomePath,".$appPackageName")
        val appDatabasePath = File(appHomePath,"sql.db")

        // need to create the directory if it doesn't exist, otherwise the database file won't be created
        appHomePath.mkdirs()

        // create the database driver
        return JdbcSqliteDriver(
            url = "jdbc:sqlite:${appDatabasePath.toURI().toASCIIString()}",
            properties = Properties(),
            schema = SqlDelightDatabase.Schema,
        )
    }
}
