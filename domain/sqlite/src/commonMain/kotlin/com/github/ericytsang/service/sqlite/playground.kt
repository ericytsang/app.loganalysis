package com.github.ericytsang.service.sqlite

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): SqlDelightDatabase {
    val driver = driverFactory.createDriver()
    val database = SqlDelightDatabase(driver)

    // Do more work with the database (see below).
    return database
}
