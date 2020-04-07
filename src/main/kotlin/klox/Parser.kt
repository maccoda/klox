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

    //expression     → comma ;
    private fun expression(): Expr {
        return comma()
    }

    // comma      -> equality ( "," equality)*
    private fun comma(): Expr {
        return leftAssociativeBinary({ equality() }, COMMA)
    }

    //equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    private fun equality(): Expr {
        return leftAssociativeBinary({ comparison() }, BANG_EQUAL, EQUAL_EQUAL)
    }


    //comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
    private fun comparison(): Expr {
        return leftAssociativeBinary({ addition() }, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)
    }

    //addition       → multiplication ( ( "-" | "+" ) multiplication )* ;
    private fun addition(): Expr {
        return leftAssociativeBinary({ multiplication() }, MINUS, PLUS)
    }

    //multiplication → unary ( ( "/" | "*" ) unary )* ;
    private fun multiplication(): Expr {
        return leftAssociativeBinary({ unary() }, STAR, SLASH)
    }

    private fun leftAssociativeBinary(rightGrammar: () -> Expr, vararg tokens: TokenType): Expr {
        var expr = rightGrammar()
        while (nextMatches(*tokens)) {
            val operator = previous()
            val right = rightGrammar()
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
