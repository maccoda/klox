package klox

class AstPrinter : ExprVisitor<String> {
    fun print(expr: Expr): String {
        return expr.accept(this)
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val builder = StringBuilder()
        builder.append("( $name")
        for (expr in exprs) {
            builder.append(" ")
            builder.append(expr.accept(this))
        }
        builder.append(")")
        return builder.toString()
    }

    override fun visitBinary(expr: Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitGrouping(expr: Grouping): String {
        return parenthesize("group", expr.expr)
    }

    override fun visitUnary(expr: Unary): String {
        return parenthesize(expr.operator.lexeme, expr.expr)
    }

    override fun visitLiteral(expr: Literal): String {
        return if (expr.value == null) "Nil" else expr.value.toString()
    }

    override fun visitTernary(expr: Ternary): String {
        return parenthesize("?", expr.condition, expr.left, expr.right)
    }

}
