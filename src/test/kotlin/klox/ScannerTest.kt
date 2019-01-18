package klox

import org.junit.Assert.assertEquals
import org.junit.Test

class ScannerTest {

    private fun semiColon(line: Int) = Token(TokenType.SEMICOLON, ";", null, line)

    private fun eof(line: Int) = Token(TokenType.EOF, "", null, line)

    @Test
    fun shouldGenerateHelloWorld() {
        val result = Scanner("print \"Hello, world!\";").scanTokens()

        assertEquals(4, result.size)
        assertEquals(Token(TokenType.PRINT, "print", null, 1), result[0])
        assertEquals(Token(TokenType.STRING, "\"Hello, world!\"", "Hello, world!", 1), result[1])
        assertEquals(semiColon(1), result[2])
        assertEquals(eof(1), result[3])
    }

    @Test
    fun shouldTokenizeIfControlFlow() {
        val result = Scanner("if (condition) {\n" +
            "  print \"yes\";\n" +
            "} else {\n" +
            "  print \"no\";\n" +
            "}").scanTokens()


        assertEquals(16, result.size)
        assertEquals(Token(TokenType.IF, "if", null, 1), result[0])
        assertEquals(leftParen(1), result[1])
        assertEquals(Token(TokenType.IDENTIFIER, "condition", null, 1), result[2])
        assertEquals(rightParen(1), result[3])
        assertEquals(leftBrace(1), result[4])
        assertEquals(print(2), result[5])
        assertEquals(Token(TokenType.STRING, "\"yes\"", "yes", 2), result[6])
        assertEquals(semiColon(2), result[7])
        assertEquals(righBrace(3), result[8])
        assertEquals(Token(TokenType.ELSE, "else", null, 3), result[9])
        assertEquals(leftBrace(3), result[10])
        assertEquals(print(4), result[11])
        assertEquals(Token(TokenType.STRING, "\"no\"", "no", 4), result[12])
        assertEquals(semiColon(4), result[13])
        assertEquals(righBrace(5), result[14])
        assertEquals(eof(5), result[15])
    }

    fun righBrace(line: Int): Token = Token(TokenType.RIGHT_BRACE, "}", null, line)

    fun print(line: Int) = Token(TokenType.PRINT, "print", null, line)

    fun leftBrace(line: Int) = Token(TokenType.LEFT_BRACE, "{", null, line)

    fun rightParen(line: Int) = Token(TokenType.RIGHT_PAREN, ")", null, line)

    fun leftParen(line: Int) = Token(TokenType.LEFT_PAREN, "(", null, line)
}
