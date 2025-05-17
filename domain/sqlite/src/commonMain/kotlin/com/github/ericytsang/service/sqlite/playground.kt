package com.github.ericytsang.service.sqlite

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(appPackageName:String): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory,appPackageName:String): SqlDelightDatabase {
    val driver = driverFactory.createDriver(appPackageName)
    val database = SqlDelightDatabase(driver)

    println(database.erictsangQueries.selectAll().executeAsList())

    // Do more work with the database (see below).
    return database
}
