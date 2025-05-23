package com.github.ericytsang.app.util

import com.github.ericytsang.logcatfilterparser.AndNode
import com.github.ericytsang.logcatfilterparser.LeafNode
import com.github.ericytsang.logcatfilterparser.Node
import com.github.ericytsang.logcatfilterparser.NotNode
import com.github.ericytsang.logcatfilterparser.OrNode

interface LogcatFilterEvaluator
{
    fun isMatch(
        logLine:String,
        logcatFilter:Node,
    ):Boolean

    companion object
    {
        fun createDefault():LogcatFilterEvaluator = LogcatFilterEvaluatorImpl()
    }
}

class LogcatFilterEvaluatorImpl:LogcatFilterEvaluator
{
    override fun isMatch(
        logLine:String,
        logcatFilter:Node,
    ):Boolean = when (logcatFilter)
    {
        is AndNode -> isMatch(logLine,logcatFilter.left) && isMatch(logLine,logcatFilter.right)
        is OrNode -> isMatch(logLine,logcatFilter.left) || isMatch(logLine,logcatFilter.right)
        is NotNode -> !isMatch(logLine, logcatFilter.child)
        is LeafNode -> isMatch(logLine, logcatFilter)
    }

    private fun isMatch(
        logLine:String,
        leafNode:LeafNode,
    ):Boolean = when
    {
        leafNode.regex -> Regex(leafNode.value).matches(logLine)
        else -> leafNode.value in logLine
    }
}