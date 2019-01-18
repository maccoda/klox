package klox

data class Token(val tokenType: TokenType, val lexeme: String, val literal: Any?, val line: Int)
