package com.github.ericytsang.logcatfilterparser

/** Abstract base class for AST nodes */
sealed class Node

/** Leaf node representing a single filter condition */
class LeafNode(

    /** The field to filter (e.g., "message", "tag", "level") */
    val key:String,

    /** The value to match */
    val value:String,

    /** True if the match is a regex (e.g., message~:...) */
    val regex:Boolean
):Node()
{
    override fun toString():String = buildString()
    {
        append(key)
        append(if (regex) "~" else "")
        append(":")
        append(if (' ' in value) "\"$value\"" else value)
    }
}

/** AND node representing a conjunction of two sub-expressions */
class AndNode(
    val left:Node,
    val right:Node,
):Node()
{
    override fun toString():String = "($left & $right)"
}

/** OR node representing a disjunction of two sub-expressions */
class OrNode(
    val left:Node,
    val right:Node,
):Node()
{
    override fun toString():String = "($left | $right)"
}

/** NOT node representing a negation of one expression */
class NotNode(
    val child:Node,
):Node()
{
    override fun toString():String = "-($child)"
}
