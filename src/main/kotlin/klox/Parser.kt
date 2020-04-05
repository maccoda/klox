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

    private fun nextMatches(vararg tokens: TokenType): Boolean {
        for (token in tokens) {
            if (check(token)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(token: TokenType): Boolean {
        return !isAtEnd() && peek().tokenType == token
    }

    private fun isAtEnd(): Boolean {
        return peek().tokenType == EOF
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
    private fun expression(): Expr {
        return equality()
    }

    //equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    private fun equality(): Expr {
        var expr = comparison()
        while (nextMatches(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Binary(expr, operator, right)
        }
        return expr
    }


    //comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
    private fun comparison(): Expr {
        var expr = addition()
        while (nextMatches(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
            val operator = previous()
            val right = addition()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    //addition       → multiplication ( ( "-" | "+" ) multiplication )* ;
    private fun addition(): Expr {
        var expr = multiplication()
        while (nextMatches(MINUS, PLUS)) {
            val operator = previous()
            val right = multiplication()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    //multiplication → unary ( ( "/" | "*" ) unary )* ;
    private fun multiplication(): Expr {
        var expr = unary()
        while (nextMatches(STAR, SLASH)) {
            val operator = previous()
            val right = unary()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    //unary          → ( "!" | "-" ) unary
    //               | primary ;
    private fun unary(): Expr {
        return if (nextMatches(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            Unary(operator, right)
        } else {
            primary()
        }
    }

    //primary        → NUMBER | STRING | "false" | "true" | "nil"
    //               | "(" expression ")" ;
    private fun primary(): Expr {
        return when {
            nextMatches(FALSE) -> Literal(false)
            nextMatches(TRUE) -> Literal(true)
            nextMatches(NIL) -> Literal(null)
            nextMatches(NUMBER, STRING) -> Literal(previous().literal)
            nextMatches(LEFT_PAREN) -> {
                val expression = expression()
                consume(RIGHT_PAREN, "Expect ')' after expression.")
                Grouping(expression)
            }
            else -> throw error(peek(), "Expected an expression.")
        }
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().tokenType == SEMICOLON) return

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
