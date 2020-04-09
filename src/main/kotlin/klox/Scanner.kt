package klox

import klox.TokenType.*


class Scanner(private val source: String) {
    private var current = 0
    private var start = 0
    private var line = 1
    private val tokens = mutableListOf<Token>()

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }
        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        val c = advance()
        when (c) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '?' -> addToken(QUESTION_MARK)
            ':' -> addToken(COLON)
            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '/' -> if (match('/')) {
                consumeLine()
            } else if (match('*')) {
                consumeMultiLineComment()
            } else {
                addToken(SLASH)
            }
            ' ', '\r', '\t' -> {
            }
            '\n' -> line++
            '"' -> consumeString()
            else ->
                when {
                    isDigit(c) -> consumeNumber()
                    isAlpha(c) -> consumeIdentifier()
                    else -> Klox.error(line, "Unexpected character")
                }
        }
    }

    private fun consumeMultiLineComment() {
        while (!isAtEnd() && !isClosingComment()) {
            val nextChar = advance()
            if (nextChar == '\n') line++
        }
        advance()
        advance()
    }

    private fun isClosingComment(): Boolean {
        return peek() == '*' && peekNext() == '/'
    }

    private fun isAlpha(c: Char): Boolean {
        return c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    }

    private fun consumeIdentifier() {
        while (isAlphaNumeric(peek())) advance()
        val text = source.substring(start until current)
        val type = keywords[text] ?: IDENTIFIER
        addToken(type)
    }

    private fun isAlphaNumeric(c: Char): Boolean {
        return isAlpha(c) || isDigit(c)
    }

    private fun consumeNumber() {
        while (isDigit(peek())) advance()
        if (peek() == '.' && isDigit(peekNext())) {
            // consume the period
            advance()
            // Consume the rest
            while (isDigit(peek())) advance()
        }
        val token = source.substring(start until current).toDouble()
        addToken(NUMBER, token)
    }


    private fun isDigit(possibleDigit: Char): Boolean {
        return possibleDigit in '0'..'9'
    }

    private fun consumeString() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }
        if (isAtEnd()) {
            Klox.error(line, "Unterminated string")
        }
        advance()

        val value = source.substring(start + 1 until current - 1)
        addToken(STRING, value)
    }

    private fun consumeLine() {
        while (peek() != '\n' && !isAtEnd()) advance()
    }

    /**
     * Return character at current index without incrementing or \0 is at end of file.
     */
    private fun peek(): Char {
        return if (isAtEnd()) '\u0000' else source[current]
    }

    /**
     * Return character at the next index without incrementing.
     */
    private fun peekNext(): Char {
        return if (current + 1 >= source.length) '\u0000' else source[current + 1]
    }

    /**
     * Move current to next character and return the current character
     */
    private fun advance(): Char {
        current++
        return source[current - 1]
    }

    /**
     * Add token with no literal value to the list
     */
    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    /**
     * Add token with the provided literal to the list
     */
    private fun addToken(type: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    /**
     * Returns true if at the end of the source input
     */
    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    /**
     * Consume the current character and progress if it matches the character provided.
     */
    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false
        current++
        return true
    }

    companion object {
        val keywords = mapOf(
            "and" to AND, "class" to CLASS, "else" to ELSE,
            "fun" to FUN, "for" to FOR, "if" to IF,
            "nil" to NIL, "or" to OR, "print" to PRINT,
            "return" to RETURN, "super" to SUPER, "this" to THIS,
            "true" to TRUE, "var" to VAR, "while" to WHILE
        )
    }
}
