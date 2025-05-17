package com.github.ericytsang.service.sqlite

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory() {
    fun createDriver(appPackageName:String): SqlDriver
}
