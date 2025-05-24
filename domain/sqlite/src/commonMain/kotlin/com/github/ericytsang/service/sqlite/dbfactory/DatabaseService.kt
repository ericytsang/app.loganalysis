package com.github.ericytsang.service.sqlite.dbfactory

import app.cash.sqldelight.TransactionWithReturn
import com.github.ericytsang.service.sqlite.LogAnalysisQueries
import com.github.ericytsang.service.sqlite.SqlDelightDatabase

interface DatabaseService
{
    fun <R> transaction(bodyWithReturn: TransactionWithReturn<R>.() -> R): R
    val queries:LogAnalysisQueries
}

internal class DatabaseServiceImpl(
    private val database:SqlDelightDatabase,
): DatabaseService
{
    override fun <R> transaction(bodyWithReturn:TransactionWithReturn<R>.()->R):R =
        database.transactionWithResult(noEnclosing = true, bodyWithReturn)

    override val queries:LogAnalysisQueries
        get() = database.logAnalysisQueries
}