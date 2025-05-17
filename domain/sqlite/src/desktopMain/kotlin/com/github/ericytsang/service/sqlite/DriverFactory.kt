package com.github.ericytsang.service.sqlite

import app.cash.sqldelight.db.SqlDriver

actual class DriverFactory {
  actual fun createDriver(): SqlDriver {
    return NativeSqliteDriver(Database.Schema, "test.db")
  }
}