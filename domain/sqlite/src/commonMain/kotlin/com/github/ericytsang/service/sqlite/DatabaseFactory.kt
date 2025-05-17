package com.github.ericytsang.service.sqlite

class DatabaseFactory()
{
    private val driverFactory = DatabaseDriverFactory()

    private val existingDatabases = mutableMapOf<String, SqlDelightDatabase>()

    fun getDatabase(appPackageName:String): SqlDelightDatabase {
        return synchronized(existingDatabases) {
            existingDatabases.getOrPut(appPackageName) {
                createDatabase(appPackageName)
            }
        }
    }

    private fun createDatabase(appPackageName:String): SqlDelightDatabase {
        val driver = driverFactory.createDriver(appPackageName)
        val database = SqlDelightDatabase(driver)
        println(database.erictsangQueries.selectAll().executeAsList())
        return database
    }
}
