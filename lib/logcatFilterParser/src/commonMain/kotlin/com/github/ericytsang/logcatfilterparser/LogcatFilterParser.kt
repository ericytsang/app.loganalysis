package com.github.ericytsang.logcatfilterparser

/** Main parser class */
class LogcatFilterParser
{
    /**
     * Parses the input string into an AST.
     */
    fun parse(input:String):Node?
    {
        val stream = Tokenizer.tokenize(input)
        val node = parseExpression(stream)
        if (stream.peek() != null)
        {
            throw RuntimeException("Unexpected token: "+stream.peek())
        }
        return node
    }

    /**
     * Parses an expression: term ( "|" term )*
     */
    private fun parseExpression(stream:TokenStream):Node?
    {
        var node = parseTerm(stream)
        while (stream.peek() == "|")
        {
            stream.next() // consume "|"
            val right = parseTerm(stream)
            node = OrNode(node,right)
        }
        return node
    }

    /**
     * Parses a term: factor ( "&" factor )*
     */
    private fun parseTerm(stream:TokenStream):Node?
    {
        var node = parseFactor(stream)
        while (stream.peek() == "&")
        {
            stream.next() // consume "&"
            val right = parseFactor(stream)
            node = AndNode(node,right)
        }
        return node
    }

    /**
     * Parses a factor: leaf | "(" expression ")"
     */
    private fun parseFactor(stream:TokenStream):Node?
    {
        val token = stream.next()
        if (token == null)
        {
            throw RuntimeException("Unexpected end of input")
        }
        if (token == "(")
        {
            val node = parseExpression(stream)
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
        var negated = false
        if (token.startsWith("-"))
        {
            negated = true
            token = token.substring(1)
        }
        if (token.startsWith("\""))
        {
            // Quoted string without key, treat as message filter
            val value = token.substring(1,token.length-1) // remove quotes
            return LeafNode(negated,"message",value,false)
        }
        else
        {
            val colonIndex = token.indexOf(":")
            if (colonIndex != -1)
            {
                var key = token.substring(0,colonIndex)
                var value = token.substring(colonIndex+1)
                var regex = false
                if (key.endsWith("~"))
                {
                    regex = true
                    key = key.substring(0,key.length-1)
                }
                if (value.startsWith("\"") && value.endsWith("\""))
                {
                    value = value.substring(1,value.length-1)
                }
                return LeafNode(negated,key,value,regex)
            }
            else
            {
                // Simple unquoted string, treat as tag filter
                return LeafNode(negated,"tag",token,false)
            }
        }
    }
}

