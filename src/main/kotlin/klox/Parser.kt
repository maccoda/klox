package klox

import klox.TokenType.*

class Parser(private val tokens: List<Token>) {
    private var current = 0

    private fun advance(): Token {
        if (!isAtEnd()) current += 1
        return tokens[current - 1]
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun isOneOfTheseTokens(vararg tokens: TokenType): Boolean {
        for (token in tokens) {
            if (check(token)) {
                return true
            }
        }
        return false
    }

    private fun check(token: TokenType): Boolean {
        return !isAtEnd() && peek().tokenType == token
    }

    private fun isAtEnd(): Boolean {
        return peek().tokenType == TokenType.EOF
    }

    private fun consume(tokenType: TokenType, msg: String): Token {
        if (check(tokenType)) return advance()
        throw error(peek(), msg)
    }

    private fun error(token: Token, msg: String): ParseError {
        Klox.error(token, msg)
        return ParseError()
    }

    //expression     → equality ;
    fun expression(): Expr {
        return equality()
    }

    //equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    private fun equality(): Expr {
        var expr = comparison()
        while (isOneOfTheseTokens(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator = advance()
            val right = comparison()
            expr = Binary(expr, operator, right)
        }
        return expr
    }


    //comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
    private fun comparison(): Expr {
        var expr = addition()
        while (isOneOfTheseTokens(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
            val operator = advance()
            val right = addition()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    //addition       → multiplication ( ( "-" | "+" ) multiplication )* ;
    private fun addition(): Expr {
        var expr = multiplication()
        while (isOneOfTheseTokens(MINUS, PLUS)) {
            val operator = advance()
            val right = multiplication()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    //multiplication → unary ( ( "/" | "*" ) unary )* ;
    private fun multiplication(): Expr {
        var expr = unary()
        while (isOneOfTheseTokens(STAR, SLASH)) {
            val operator = advance()
            val right = unary()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    //unary          → ( "!" | "-" ) unary
    //               | primary ;
    private fun unary(): Expr {
        return if (isOneOfTheseTokens(BANG, MINUS)) {
            val operator = advance()
            val right = unary()
            Unary(operator, right)
        } else {
            primary()
        }
    }

    //primary        → NUMBER | STRING | "false" | "true" | "nil"
    //               | "(" expression ")" ;
    private fun primary(): Expr {
        val expr: Expr? =
            when {
                isOneOfTheseTokens(FALSE) -> Literal(false)
                isOneOfTheseTokens(TRUE) -> Literal(true)
                isOneOfTheseTokens(NIL) -> Literal(null)
                isOneOfTheseTokens(NUMBER, STRING) -> Literal(peek().literal)
                isOneOfTheseTokens(LEFT_PAREN) -> {
                    val expression = expression()
                    consume(RIGHT_PAREN, "Expect ')' after expression.")
                    Grouping(expression)
                }
                else -> throw error(peek(), "Expected an expression.")
            }
        advance()
        return expr!!
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().tokenType === klox.TokenType.SEMICOLON) return

            when (peek().tokenType) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> {
                    return
                }
                else -> advance()
            }
        }
    }

    fun parse(): Expr? {
        return try {
            expression()
        } catch (error: ParseError) {
            null
        }
    }

    companion object {
        private class ParseError : RuntimeException()
    }

}
