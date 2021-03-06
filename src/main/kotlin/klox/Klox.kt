package klox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    when {
        args.size > 1 -> {
            println("Usage klox [script]")
            exitProcess(64)
        }
        args.size == 1 -> Klox.runFile(args[0])
        else -> Klox.runPrompt()
    }
}

object Klox {
    private var hadError = false
    private var hadRuntimeError = false
    private val interpreter = Interpreter()

    fun runPrompt() {
        val reader = BufferedReader(InputStreamReader(System.`in`))
        while (true) {
            print("> ")
            runOn(reader.readLine())
            hadError = false
        }
    }

    fun runtimeError(error: RuntimeError) {
        System.err.println(
            error.message +
                    "\n[line " + error.token.line + "]"
        )
        hadRuntimeError = true
    }

    fun runFile(path: String) {
        val myPath = ClassLoader.getSystemResource(path).toURI()
        val bytes = Files.readAllBytes(Paths.get(myPath))
        runOn(String(bytes, Charset.defaultCharset()))
        if (hadError) exitProcess(65)
        if (hadRuntimeError) exitProcess(70)
    }

    private fun runOn(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        val expression = parser.parse()
        // Stop as we had a syntax error
        if (hadError || expression == null) return

        interpreter.interpret(expression)
    }

    private fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error$where: $message")
        hadError = true
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    fun error(token: Token, message: String) {
        if (token.tokenType === TokenType.EOF) {
            report(token.line, " at end", message)
        } else {
            report(token.line, " at '${token.lexeme}'", message)
        }
    }
}
