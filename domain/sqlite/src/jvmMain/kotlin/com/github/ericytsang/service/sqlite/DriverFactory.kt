package com.github.ericytsang.service.sqlite

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.util.Properties
import app.cash.sqldelight.db.SqlDriver

actual class DriverFactory {
  actual fun createDriver(): SqlDriver {
    val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:test.db", Properties(),SqlDelightDatabase.Schema)
    return driver
  }
}
