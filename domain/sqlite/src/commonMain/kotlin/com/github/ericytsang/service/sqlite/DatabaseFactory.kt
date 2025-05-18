package com.github.ericytsang.service.sqlite

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
        val database = SqlDelightDatabase(driver)
        return database
    }
}
