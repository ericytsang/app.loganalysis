package com.github.ericytsang.logcatfilterparser

internal object Tokenizer
{
    fun tokenize(input:String):TokenStream
    {
        val tokens = tokenizeInternal(input).insertOrOperators()
        if (PRINT_DEBUG) println(tokens)
        return TokenStream(tokens)
    }

    private fun List<String>.insertOrOperators():List<String>
    {
        return fold(emptyList<String>())
        { acc,token ->
            if (isLeafToken(acc.lastOrNull()) && isLeafToken(token))
            {
                acc+"|"+token
            }
            else
            {
                acc+token
            }
        }
    }

    /**
     * Tokenizes the input string into a list of tokens.
     * Handles quoted strings and separates operators and brackets.
     */
    private fun tokenizeInternal(input:String):List<String>
    {
        val tokens:MutableList<String> = mutableListOf<String>()
        val buffer = StringBuilder()
        var inQuote = false

        for (i in 0..<input.length)
        {
            val c = input[i]
            if (c == '"')
            {
                if (inQuote)
                {
                    buffer.append(c)
                    tokens.add(buffer.toString())
                    buffer.setLength(0)
                    inQuote = false
                }
                else
                {
                    buffer.append(c)
                    inQuote = true
                }
            }
            else if (inQuote)
            {
                buffer.append(c)
            }
            else if (c == ' ')
            {
                if (buffer.isNotEmpty())
                {
                    tokens.add(buffer.toString())
                    buffer.setLength(0)
                }
            }
            else if (c in "&|()-")
            {
                if (buffer.isNotEmpty())
                {
                    tokens.add(buffer.toString())
                    buffer.setLength(0)
                }
                tokens.add(c.toString())
            }
            else
            {
                buffer.append(c)
            }
        }
        if (buffer.isNotEmpty())
        {
            tokens.add(buffer.toString())
        }
        return tokens
    }

    private fun isLeafToken(token:String?):Boolean
    {
        return token != null && !(token.length == 1 && token[0] in OPERATORS)
    }

    private const val OPERATORS = "&|()-"

    private const val PRINT_DEBUG = false
}
