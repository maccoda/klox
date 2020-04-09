package klox

sealed class Expr {
    abstract fun <R> accept(visitor: ExprVisitor<R>): R
}

data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr() {
    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visitBinary(this)
    }
}

data class Grouping(val expr: Expr) : Expr() {
    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visitGrouping(this)
    }
}

data class Unary(val operator: Token, val expr: Expr) : Expr() {
    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visitUnary(this)
    }
}

data class Literal(val value: Any?) : Expr() {
    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visitLiteral(this)
    }
}

data class Ternary(val condition: Expr, val left: Expr, val right: Expr) : Expr() {
    override fun <R> accept(visitor: ExprVisitor<R>): R {
        return visitor.visitTernary(this)
    }
}

interface ExprVisitor<R> {
    fun visitBinary(expr: Binary): R
    fun visitGrouping(expr: Grouping): R
    fun visitUnary(expr: Unary): R
    fun visitLiteral(expr: Literal): R
    fun visitTernary(expr: Ternary): R
}
