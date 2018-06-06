package hachi.lang.domain.expression

import hachi.lang.bytecode.generator.ExpressionGenerator
import hachi.lang.bytecode.generator.StatementGenerator
import hachi.lang.domain.global.CompareSign
import hachi.lang.domain.type.BuiltInType
import hachi.lang.domain.type.Type

class ConditionalExpression(val leftExpression: Expression, val rightExpression: Expression, val compareSign: CompareSign) : Expression {
    val type = BuiltInType.BOOLEAN

    override fun getType(): Type {
        return this.type
    }

    override fun accept(expressionGenerator: ExpressionGenerator) {
        expressionGenerator.generate(this)
    }

    override fun accept(statementGenerator: StatementGenerator) {
        statementGenerator.generate(this)
    }
}