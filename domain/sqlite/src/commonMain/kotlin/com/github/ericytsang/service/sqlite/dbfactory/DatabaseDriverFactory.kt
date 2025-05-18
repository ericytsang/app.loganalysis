package com.github.ericytsang.service.sqlite.dbfactory

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory() {
    fun createDriver(appPackageName:String): SqlDriver
}
