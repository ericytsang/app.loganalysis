package com.github.ericytsang.logcatfilterparser

/** Main parser class */
class LogcatFilterParser
{
    /**
     * Parses the input string into an AST.
     */
    fun parse(
        input:String,
        forceIsCaseSensitive:Boolean = false,
    ):Node
    {
        val stream = Tokenizer.tokenize(input)
        val node = parseOrExpression(stream, forceIsCaseSensitive)
        if (stream.peek() != null)
        {
            throw RuntimeException("Unexpected token: "+stream.peek())
        }
        return node
    }

    /**
     * Parses an expression: term ( "|" term )
     */
    private fun parseOrExpression(stream:TokenStream,forceIsCaseSensitive:Boolean):Node
    {
        var node = parseAndExpression(stream,forceIsCaseSensitive)
        while (stream.peek() == "|")
        {
            stream.next() // consume "|"
            val right = parseAndExpression(stream,forceIsCaseSensitive)
            node = OrNode(node,right)
        }
        return node
    }

    /**
     * Parses a term: factor ( "&" factor )
     */
    private fun parseAndExpression(stream:TokenStream,forceIsCaseSensitive:Boolean):Node
    {
        var node = parseNotExpression(stream,forceIsCaseSensitive)
        while (stream.peek() == "&")
        {
            stream.next() // consume "&"
            val right = parseNotExpression(stream,forceIsCaseSensitive)
            node = AndNode(node,right)
        }
        return node
    }

    /**
     * Parses an expression: term ( "|" term )
     */
    private fun parseNotExpression(stream:TokenStream,forceIsCaseSensitive:Boolean):Node
    {
        var node: Node? = null
        while (stream.peek() == "-")
        {
            stream.next() // consume "-"
            val right = parseBrackets(stream,forceIsCaseSensitive)
            node = NotNode(right)
        }
        if (node == null)
        {
            node = parseBrackets(stream,forceIsCaseSensitive)
        }
        return node
    }

    /**
     * Parses a factor: leaf | "(" expression ")"
     */
    private fun parseBrackets(stream:TokenStream,forceIsCaseSensitive:Boolean):Node
    {
        val token = stream.next()
        if (token == null)
        {
            throw RuntimeException("Unexpected end of input")
        }
        if (token == "(")
        {
            val node = parseOrExpression(stream,forceIsCaseSensitive)
            val close = stream.next()
            if (close != ")")
            {
                throw RuntimeException("Expected ')'")
            }
            return node
        }
        else if (isLeafToken(token))
        {
            return parseLeaf(token,forceIsCaseSensitive)
        }
        else
        {
            throw RuntimeException("Unexpected token: $token")
        }
    }

    /**
     * Checks if a token is a leaf (not an operator or bracket).
     */
    private fun isLeafToken(token:String):Boolean
    {
        return (token != "&") && (token != "|") && (token != "(") && (token != ")")
    }

    /**
     * Parses a leaf token into a LeafNode.
     */
    private fun parseLeaf(token:String, forceIsCaseSensitive:Boolean):LeafNode
    {
        var token = token
        val colonIndex by lazy { token.indexOf(":") }

        // Quoted string without key, treat as message filter
        return if (getQuotedStringValue(token) != token)
        {
            val value = getQuotedStringValue(token)
            LeafNode(
                key = "message",
                value = value,
                regex = false,
                caseSensitive = false || forceIsCaseSensitive,
            )
        }
        else if (colonIndex != -1)
        {
            var key = token.substring(0,colonIndex)
            var value = token.substring(colonIndex+1)
            var regex = false
            var caseSensitive = false || forceIsCaseSensitive
            while (key.endsWith("~") || key.endsWith("^"))
            {
                when
                {
                    key.endsWith("~") -> regex = true
                    key.endsWith("^") -> caseSensitive = true
                }
                key = key.substring(0,key.length-1)
            }
            value = getQuotedStringValue(value)
            LeafNode(
                key = key,
                value = value,
                regex = regex,
                caseSensitive = caseSensitive,
            )
        }
        else
        {
            // Simple unquoted string, treat as message filter
            LeafNode(
                key = "message",
                value = token,
                regex = false,
                caseSensitive = false || forceIsCaseSensitive,
            )
        }
    }

    /**
     * Extracts the value from a quoted string, removing the quotes.
     * If the token is not quoted, it returns the token as is.
     * This handles both single and double quotes.
     */
    private fun getQuotedStringValue(token:String):String
    {
        return when
        {
            token.startsWith("\"") && token.endsWith("\"") -> token.substring(1,token.length-1)
            token.startsWith("'") && token.endsWith("'") -> token.substring(1,token.length-1)
            else -> token
        }
    }
}

