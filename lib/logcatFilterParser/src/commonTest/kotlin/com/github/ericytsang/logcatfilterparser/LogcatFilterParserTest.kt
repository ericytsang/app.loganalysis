package com.github.ericytsang.logcatfilterparser

import org.junit.Test

class LogcatFilterParserTest
{
    @Test
    fun `test message key`()
    {
        `test parser`("message:hello")
    }

    @Test
    fun `test tag key with quotations`()
    {
        `test parser`("tag:\"goodbye friend\"")
    }

    @Test
    fun `test level key`()
    {
        `test parser`("level:error")
    }

    @Test
    fun `test no key`()
    {
        `test parser`("simplenotag")
    }

    @Test
    fun `test no key with quotations`()
    {
        `test parser`("\"simple no tag\"")
    }

    @Test
    fun `test negated message key with quotations`()
    {
        `test parser`("-message:\"i don\'t want to see this in the output\"")
    }

    @Test
    fun `test negated message key with regular expression`()
    {
        `test parser`("-message~:reg.*exp")
    }

    @Test
    fun `test complex expression with operators and brackets`()
    {
        `test parser`("tag:foo & (level:error | message:\"bar\")")
    }

    fun `test parser`(input:String)
    {
        val parser = LogcatFilterParser()
        println("input: $input")
        val result = parser.parse(input)
        println("result: $result")
    }

}