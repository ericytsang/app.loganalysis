package com.github.ericytsang.service.sqlite.dbfactory

import app.cash.sqldelight.db.SqlDriver
import com.github.ericytsang.service.sqlite.SqlDelightDatabase

interface DatabaseFactory
{
    fun getDatabase(appPackageName:String):DatabaseService

    companion object
    {
        fun createDefault():DatabaseFactory = DatabaseFactoryCreateCacheImpl(
            delegate = DatabaseFactoryCreateNewInstanceImpl(),
        )
    }
}

internal class DatabaseFactoryCreateCacheImpl(
    private val delegate: DatabaseFactory,
):DatabaseFactory
{
    private val existingDatabases = mutableMapOf<String,DatabaseService>()

    override fun getDatabase(appPackageName:String):DatabaseService =
        getSqlDelightDatabase(appPackageName)

    /**
     * returns a database connection to a [SqlDelightDatabase] SQLite DB.
     * if the database connection already exists, it will be reused.
     */
    private fun getSqlDelightDatabase(
        appPackageName:String,
    ):DatabaseService = synchronized(existingDatabases) {
        existingDatabases.getOrPut(appPackageName) {
            delegate.getDatabase(appPackageName)
        }
    }
}


internal class DatabaseFactoryCreateNewInstanceImpl:DatabaseFactory
{
    private val driverFactory = DatabaseDriverFactory()

    override fun getDatabase(appPackageName:String):DatabaseService =
        DatabaseServiceImpl(createDatabase(appPackageName))

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
