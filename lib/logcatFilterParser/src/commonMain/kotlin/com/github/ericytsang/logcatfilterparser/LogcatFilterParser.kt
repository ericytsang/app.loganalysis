package com.github.ericytsang.logcatfilterparser

/** Main parser class */
class LogcatFilterParser
{
    /**
     * Parses the input string into an AST.
     */
    fun parse(input:String):Node
    {
        val stream = Tokenizer.tokenize(input)
        val node = parseOrExpression(stream)
        if (stream.peek() != null)
        {
            throw RuntimeException("Unexpected token: "+stream.peek())
        }
        return node
    }

    /**
     * Parses an expression: term ( "|" term )
     */
    private fun parseOrExpression(stream:TokenStream):Node
    {
        var node = parseAndExpression(stream)
        while (stream.peek() == "|")
        {
            stream.next() // consume "|"
            val right = parseAndExpression(stream)
            node = OrNode(node,right)
        }
        return node
    }

    /**
     * Parses a term: factor ( "&" factor )
     */
    private fun parseAndExpression(stream:TokenStream):Node
    {
        var node = parseNotExpression(stream)
        while (stream.peek() == "&")
        {
            stream.next() // consume "&"
            val right = parseNotExpression(stream)
            node = AndNode(node,right)
        }
        return node
    }

    /**
     * Parses an expression: term ( "|" term )
     */
    private fun parseNotExpression(stream:TokenStream):Node
    {
        var node: Node? = null
        while (stream.peek() == "-")
        {
            stream.next() // consume "-"
            val right = parseBrackets(stream)
            node = NotNode(right)
        }
        if (node == null)
        {
            node = parseBrackets(stream)
        }
        return node
    }

    /**
     * Parses a factor: leaf | "(" expression ")"
     */
    private fun parseBrackets(stream:TokenStream):Node
    {
        val token = stream.next()
        if (token == null)
        {
            throw RuntimeException("Unexpected end of input")
        }
        if (token == "(")
        {
            val node = parseOrExpression(stream)
            val close = stream.next()
            if (close != ")")
            {
                throw RuntimeException("Expected ')'")
            }
            return node
        }
        else if (isLeafToken(token))
        {
            return parseLeaf(token)
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
    private fun parseLeaf(token:String):LeafNode
    {
        var token = token
        if (token.startsWith("\""))
        {
            // Quoted string without key, treat as message filter
            val value = token.substring(1,token.length-1) // remove quotes
            return LeafNode(
                key = "message",
                value = value,
                regex = false,
                caseSensitive = false,
            )
        }
        else
        {
            val colonIndex = token.indexOf(":")
            if (colonIndex != -1)
            {
                var key = token.substring(0,colonIndex)
                var value = token.substring(colonIndex+1)
                var regex = false
                var caseSensitive = false
                while (key.endsWith("~") || key.endsWith("^"))
                {
                    when
                    {
                        key.endsWith("~") -> regex = true
                        key.endsWith("^") -> caseSensitive = true
                    }
                    key = key.substring(0,key.length-1)
                }
                if (value.startsWith("\"") && value.endsWith("\""))
                {
                    value = value.substring(1,value.length-1)
                }
                return LeafNode(
                    key = key,
                    value = value,
                    regex = regex,
                    caseSensitive = caseSensitive,
                )
            }
            else
            {
                // Simple unquoted string, treat as message filter
                return LeafNode(
                    key = "message",
                    value = token,
                    regex = false,
                    caseSensitive = false,
                )
            }
        }
    }
}

