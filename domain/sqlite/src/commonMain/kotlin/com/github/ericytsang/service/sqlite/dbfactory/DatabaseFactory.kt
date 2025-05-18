package com.github.ericytsang.service.sqlite.dbfactory

import app.cash.sqldelight.db.SqlDriver
import com.github.ericytsang.service.sqlite.SqlDelightDatabase

class DatabaseFactory()
{
    private val driverFactory = DatabaseDriverFactory()

    private val existingDatabases = mutableMapOf<String,SqlDelightDatabase>()

    /**
     * returns a database connection to a [SqlDelightDatabase] SQLite DB.
     * if the database connection already exists, it will be reused.
     */
    fun getDatabase(appPackageName:String):SqlDelightDatabase
    {
        return synchronized(existingDatabases) {
            existingDatabases.getOrPut(appPackageName) {
                createDatabase(appPackageName)
            }
        }
    }

    /**
     * creates a new database connection to a [SqlDelightDatabase] SQLite DB.
     */
    private fun createDatabase(appPackageName:String):SqlDelightDatabase
    {
        val driver = driverFactory.createDriver(appPackageName)
        configureDatabase(driver)
        val database = SqlDelightDatabase.Companion(driver)
        return database
    }

    private fun configureDatabase(databaseDriver: SqlDriver)
    {

        // enable WAL mode for better performance
        databaseDriver.execute(null, "PRAGMA journal_mode=WAL;", 0)

        // extend busy timeout to 10 seconds as multiple processes may access the database
        databaseDriver.execute(null, "PRAGMA busy_timeout=10000;", 0)
    }
}
