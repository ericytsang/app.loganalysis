package com.github.ericytsang.service.sqlite.dbfactory

import app.cash.sqldelight.db.SqlDriver

internal expect class DatabaseDriverFactory() {
    fun createDriver(appPackageName:String): SqlDriver
}
