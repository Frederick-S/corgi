package hachi.lang.parsing.visitor

import hachi.antlr.HachiBaseVisitor
import hachi.antlr.HachiParser
import hachi.lang.domain.scope.FunctionSignature
import hachi.lang.domain.scope.Scope
import hachi.lang.parsing.visitor.expression.ExpressionVisitor
import hachi.lang.parsing.visitor.expression.function.FunctionParameterListExpressionVisitor
import hachi.lang.util.TypeResolver

class FunctionSignatureVisitor(val scope: Scope) : HachiBaseVisitor<FunctionSignature>() {
    private val expressionVisitor = ExpressionVisitor(this.scope)

    override fun visitFunctionDeclaration(functionDeclarationContext: HachiParser.FunctionDeclarationContext): FunctionSignature {
        val functionName = functionDeclarationContext.functionName().text
        val returnType = TypeResolver.getFromTypeContext(functionDeclarationContext.type())
        val functionParameterListContext = functionDeclarationContext.functionParameterList()

        return if (functionParameterListContext != null) {
            val parameters = functionParameterListContext.accept(FunctionParameterListExpressionVisitor(this.expressionVisitor))

            FunctionSignature(functionName, parameters, returnType)
        } else {
            FunctionSignature(functionName, emptyList(), returnType)
        }
    }
}