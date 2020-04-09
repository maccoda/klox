package klox

class RpnPrinter : ExprVisitor<String> {
    fun print(expr: Expr): String {
        return expr.accept(this)
    }

    override fun visitBinary(expr: Binary): String {
        return "${expr.left.accept(this)} ${expr.right.accept(this)} ${expr.operator.lexeme}"
    }

    override fun visitGrouping(expr: Grouping): String {
        return expr.expr.accept(this)
    }

    override fun visitUnary(expr: Unary): String {
        return "${expr.operator.lexeme} ${expr.expr.accept(this)}"
    }

    override fun visitLiteral(expr: Literal): String {
        return if (expr.value == null) "Nil" else expr.value.toString()
    }

    override fun visitTernary(expr: Ternary): String {
        return "ternary"
    }

}
