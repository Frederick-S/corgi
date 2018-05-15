package corgi.lang.domain.scope

import corgi.lang.domain.expressions.FunctionParameter
import corgi.lang.domain.type.Type

class FunctionSignature(val name: String, val arguments: List<FunctionParameter>, val returnType: Type)