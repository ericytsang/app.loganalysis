package com.github.ericytsang.logcatfilterparser

/** Helper class to manage the token stream during parsing */
internal class TokenStream(
    private val tokens:List<String?>,
)
{
    private var index:Int = 0

    fun peek():String?
    {
        if (index < tokens.size)
        {
            return tokens[index]
        }
        return null
    }

    fun next():String?
    {
        if (index < tokens.size)
        {
            return tokens[index++]
        }
        return null
    }
}
