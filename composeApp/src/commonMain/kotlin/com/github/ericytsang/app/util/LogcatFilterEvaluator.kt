package com.github.ericytsang.app.util

import com.github.ericytsang.logcatfilterparser.AndNode
import com.github.ericytsang.logcatfilterparser.LeafNode
import com.github.ericytsang.logcatfilterparser.Node
import com.github.ericytsang.logcatfilterparser.NotNode
import com.github.ericytsang.logcatfilterparser.OrNode

interface LogcatFilterEvaluator
{
    /**
     * evaluates whether a log line matches the given logcat filter.
     * @param caseSensitive Whether the match should be case-sensitive. otherwise,
     * it will use the case-sensitivity of the leaf node.
     * @param logLine The log line to evaluate against the [logcatFilter].
     * @param logcatFilter The logcat filter to evaluate against.
     */
    fun isMatch(
        caseSensitive:Boolean,
        logLine:String,
        logcatFilter:Node,
    ):Boolean
}

class LogcatFilterEvaluatorImpl:LogcatFilterEvaluator
{
    override fun isMatch(
        caseSensitive:Boolean,
        logLine:String,
        logcatFilter:Node,
    ):Boolean = when (logcatFilter)
    {
        is AndNode -> isMatch(caseSensitive,logLine,logcatFilter.left) && isMatch(caseSensitive,logLine,logcatFilter.right)
        is OrNode -> isMatch(caseSensitive,logLine,logcatFilter.left) || isMatch(caseSensitive,logLine,logcatFilter.right)
        is NotNode -> !isMatch(caseSensitive,logLine, logcatFilter.child)
        is LeafNode -> isMatch(caseSensitive,logLine,logcatFilter)
    }

    private fun isMatch(
        caseSensitive:Boolean,
        logLine:String,
        leafNode:LeafNode,
    ):Boolean = isMatch2(
        logLine = logLine,
        leafNode = leafNode,
        caseSensitive = leafNode.caseSensitive || caseSensitive,
    )

    private fun isMatch2(
        caseSensitive:Boolean,
        logLine:String,
        leafNode:LeafNode,
    ):Boolean = when
    {
        leafNode.regex && caseSensitive -> runCatching { Regex(leafNode.value).containsMatchIn(logLine) }.getOrElse { false }
        leafNode.regex -> runCatching { Regex(leafNode.value,RegexOption.IGNORE_CASE).containsMatchIn(logLine) }.getOrElse { false }
        caseSensitive -> leafNode.value in logLine
        else -> leafNode.value.lowercase() in logLine.lowercase()
    }
}
