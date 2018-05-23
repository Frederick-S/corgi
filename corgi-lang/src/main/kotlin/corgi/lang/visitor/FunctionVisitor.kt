package corgi.lang.visitor

import corgi.antlr.CorgiBaseVisitor
import corgi.antlr.CorgiParser
import corgi.lang.domain.`class`.Function
import corgi.lang.domain.expression.FunctionParameter
import corgi.lang.domain.scope.LocalVariable
import corgi.lang.domain.scope.Scope
import corgi.lang.domain.statement.Statement
import corgi.lang.domain.type.Type
import corgi.lang.util.TypeResolver

class FunctionVisitor : CorgiBaseVisitor<Function> {
    var scope: Scope

    constructor(scope: Scope) {
        this.scope = Scope(scope)
    }

    override fun visitFunction(functionContext: CorgiParser.FunctionContext): Function {
        val functionName = this.getName(functionContext)
        val returnType = this.getReturnType(functionContext)
        val arguments = this.getArguments(functionContext)
        val block = this.getBlock(functionContext)

        return Function(functionName, arguments, block, returnType)
    }

    private fun getName(functionContext: CorgiParser.FunctionContext): String {
        return functionContext.functionDeclaration().functionName().text
    }

    private fun getReturnType(functionContext: CorgiParser.FunctionContext): Type {
        val typeContext = functionContext.functionDeclaration().type()

        return TypeResolver.getFromTypeName(typeContext)
    }

    private fun getArguments(functionContext: CorgiParser.FunctionContext): List<FunctionParameter> {
        val functionArgumentContexts = functionContext.functionDeclaration().functionArgument()

        val functionParameters = functionArgumentContexts.map {
            FunctionParameter(it.ID().text, TypeResolver.getFromTypeName(it.type()))
        }

        functionParameters.forEach { this.scope.addLocalVariable(LocalVariable(it.name, it.type)) }

        return functionParameters
    }

    private fun getBlock(functionContext: CorgiParser.FunctionContext): Statement {
        val statementVisitor = StatementVisitor(this.scope)

        return functionContext.block().accept(statementVisitor)
    }
}