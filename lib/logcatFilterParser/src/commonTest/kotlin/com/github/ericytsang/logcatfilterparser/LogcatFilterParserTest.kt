package com.github.ericytsang.logcatfilterparser

import org.junit.Test

class LogcatFilterParserTest
{
    @Test
    fun `test 1`()
    {
        `test parser`("message:hello")
    }

    @Test
    fun `test 2`()
    {
        `test parser`("tag:\"goodbye friend\"")
    }

    @Test
    fun `test 3`()
    {
        `test parser`("level:error")
    }

    @Test
    fun `test 4`()
    {
        `test parser`("simplenotag")
    }

    @Test
    fun `test 5`()
    {
        `test parser`("\"simple no tag\"")
    }

    @Test
    fun `test 6`()
    {
        `test parser`("-message:\"i don\'t want to see this in the output\"")
    }

    @Test
    fun `test 7`()
    {
        `test parser`("-message~:reg.*exp")
    }

    @Test
    fun `test 8`()
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