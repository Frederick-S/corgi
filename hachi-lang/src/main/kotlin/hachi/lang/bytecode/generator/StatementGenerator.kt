package hachi.lang.bytecode.generator

import hachi.lang.domain.expression.ConditionalExpression
import hachi.lang.domain.expression.ConstructorCall
import hachi.lang.domain.expression.EmptyExpression
import hachi.lang.domain.expression.FunctionCall
import hachi.lang.domain.expression.FunctionParameter
import hachi.lang.domain.expression.SuperCall
import hachi.lang.domain.expression.Value
import hachi.lang.domain.expression.VariableReference
import hachi.lang.domain.global.CompareSign
import hachi.lang.domain.math.Addition
import hachi.lang.domain.math.Division
import hachi.lang.domain.math.Multiplication
import hachi.lang.domain.math.Subtraction
import hachi.lang.domain.scope.Scope
import hachi.lang.domain.statement.AssignmentStatement
import hachi.lang.domain.statement.BlockStatement
import hachi.lang.domain.statement.IfStatement
import hachi.lang.domain.statement.PrintStatement
import hachi.lang.domain.statement.RangedForStatement
import hachi.lang.domain.statement.ReturnStatement
import hachi.lang.domain.statement.VariableDeclarationStatement
import hachi.lang.domain.type.ClassType
import jdk.internal.org.objectweb.asm.Label
import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes

class StatementGenerator(private val methodVisitor: MethodVisitor, val scope: Scope) {
    private val expressionGenerator = ExpressionGenerator(methodVisitor, scope)

    fun generate(printStatement: PrintStatement) {
        this.methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")

        val expression = printStatement.expression
        expression.accept(expressionGenerator)

        val type = expression.getType()
        val descriptor = "(" + type.getDescriptor() + ")V"
        val owner = ClassType("java.io.PrintStream")
        val fieldDescriptor = owner.getDescriptor()

        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, fieldDescriptor, "println", descriptor, false)
    }

    fun generate(variableDeclarationStatement: VariableDeclarationStatement) {
        val expression = variableDeclarationStatement.expression

        expression.accept(this.expressionGenerator)

        val assignmentStatement = AssignmentStatement(variableDeclarationStatement)

        this.generate(assignmentStatement)
    }

    fun generate(assignmentStatement: AssignmentStatement) {
        val variableName = assignmentStatement.variableName
        val type = assignmentStatement.expression.getType()
        val index = this.scope.getLocalVariableIndex(variableName)

        this.methodVisitor.visitVarInsn(type.getStoreOpcode(), index)
    }

    fun generate(functionCall: FunctionCall) {
        functionCall.accept(this.expressionGenerator)
    }

    fun generate(returnStatement: ReturnStatement) {
        val expression = returnStatement.expression

        expression.accept(this.expressionGenerator)

        this.methodVisitor.visitInsn(expression.getType().getReturnOpcode())
    }

    fun generate(blockStatement: BlockStatement) {
        val scope = blockStatement.scope
        val statements = blockStatement.statements
        val statementGenerator = StatementGenerator(this.methodVisitor, scope)

        statements.forEach { it.accept(statementGenerator) }
    }

    fun generate(ifStatement: IfStatement) {
        ifStatement.condition.accept(this.expressionGenerator)

        val trueLabel = Label()
        val falseLabel = Label()

        this.methodVisitor.visitJumpInsn(Opcodes.IFNE, trueLabel)

        ifStatement.falseStatement.let {
            it?.accept(this)
        }

        this.methodVisitor.visitJumpInsn(Opcodes.GOTO, falseLabel)
        this.methodVisitor.visitLabel(trueLabel)

        ifStatement.trueStatement.accept(this)

        this.methodVisitor.visitLabel(falseLabel)
    }

    fun generate(rangedForStatement: RangedForStatement) {
        val newScope = rangedForStatement.scope
        val statementGenerator = StatementGenerator(this.methodVisitor, newScope)
        val expressionGenerator = ExpressionGenerator(this.methodVisitor, newScope)
        val iterator = rangedForStatement.iteratorVariable
        val incrementSection = Label()
        val decrementSection = Label()
        val endLoopSection = Label()
        val iteratorVariableName = rangedForStatement.iteratorVariableName
        val endExpression = rangedForStatement.endExpression
        val iteratorVariable = VariableReference(iteratorVariableName, rangedForStatement.getType())
        val iteratorGreaterThanEndCondition = ConditionalExpression(iteratorVariable, endExpression, CompareSign.GREATER_THAN)
        val iteratorLessThanEndCondition = ConditionalExpression(iteratorVariable, endExpression, CompareSign.LESS_THAN)

        iterator.accept(statementGenerator)

        iteratorLessThanEndCondition.accept(expressionGenerator)
        this.methodVisitor.visitJumpInsn(Opcodes.IFNE, incrementSection)

        iteratorGreaterThanEndCondition.accept(expressionGenerator)
        this.methodVisitor.visitJumpInsn(Opcodes.IFNE, decrementSection)

        this.methodVisitor.visitLabel(incrementSection)
        rangedForStatement.statement.accept(statementGenerator)
        this.methodVisitor.visitIincInsn(newScope.getLocalVariableIndex(iteratorVariableName), 1)
        iteratorGreaterThanEndCondition.accept(expressionGenerator)
        this.methodVisitor.visitJumpInsn(Opcodes.IFEQ, incrementSection)
        this.methodVisitor.visitJumpInsn(Opcodes.GOTO, endLoopSection)

        this.methodVisitor.visitLabel(decrementSection)
        rangedForStatement.statement.accept(statementGenerator)
        this.methodVisitor.visitIincInsn(newScope.getLocalVariableIndex(iteratorVariableName), -1)
        iteratorLessThanEndCondition.accept(expressionGenerator)
        this.methodVisitor.visitJumpInsn(Opcodes.IFEQ, decrementSection)

        this.methodVisitor.visitLabel(endLoopSection)
    }

    fun generate(superCall: SuperCall) {
        this.expressionGenerator.generate(superCall)
    }

    fun generate(constructorCall: ConstructorCall) {
        this.expressionGenerator.generate(constructorCall)
    }

    fun generate(addition: Addition) {
        this.expressionGenerator.generate(addition)
    }

    fun generate(subtraction: Subtraction) {
        this.expressionGenerator.generate(subtraction)
    }

    fun generate(multiplication: Multiplication) {
        this.expressionGenerator.generate(multiplication)
    }

    fun generate(division: Division) {
        this.expressionGenerator.generate(division)
    }

    fun generate(functionParameter: FunctionParameter) {
        this.expressionGenerator.generate(functionParameter)
    }

    fun generate(conditionalExpression: ConditionalExpression) {
        this.expressionGenerator.generate(conditionalExpression)
    }

    fun generate(value: Value) {
        this.expressionGenerator.generate(value)
    }

    fun generate(variableReference: VariableReference) {
        this.expressionGenerator.generate(variableReference)
    }

    fun generate(emptyExpression: EmptyExpression) {
        this.expressionGenerator.generate(emptyExpression)
    }
}