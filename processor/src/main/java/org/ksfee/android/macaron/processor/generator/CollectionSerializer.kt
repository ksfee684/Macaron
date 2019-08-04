package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

class CollectionSerializer(
    val model: CollectionModel
) {

    private val className: String = model.className + SERIALIZER_CLASS_SUFFIX

    fun write() {
        val file = FileSpec.builder(model.packageName, className)
            .addImport("org.ksfee.android.macaron.library", "typedValue")
            .addType(buildSerializer())
            .addType(buildValidator())
            .build()

        file.writeTo(model.context.outDir)
    }

    private fun buildSerializer(): TypeSpec {
        return TypeSpec.objectBuilder("${model.className}Serializer")
            .addFunction(buildSerializeFunc())
            .build()
    }

    private fun buildSerializeFunc(): FunSpec {
        val parameters = model.fields.joinToString(", \n") {
            "data.typedValue(\"${it.simpleName}\") as ${it.javaToKotlinType()}"
        }

        return FunSpec.builder("serialize")
            .returns(model.type)
            .addParameter("data", Map::class.asClassName().parameterizedBy(String::class.asTypeName(), Any::class.asTypeName()))
            .addStatement("%N.%N(data)", buildValidator(), buildValidateMethod())
            .addStatement("return %T(", model.type)
            .addStatement(parameters)
            .addStatement(")")
            .build()
    }

    private fun buildValidator(): TypeSpec {
        return TypeSpec.objectBuilder("${model.className}Validator")
            .addFunction(buildValidateMethod())
            .build()
    }

    private fun buildValidateMethod(): FunSpec {
        val args = mutableListOf<Any>()

        val statement = model.fields.map {
            args.add(it.simpleName)
            args.add(it.javaToKotlinType(false))
            "%S to %T::class"
        }.joinToString(", ")

        return FunSpec.builder("validateData")
            .addParameter("data", Map::class.asClassName().parameterizedBy(String::class.asTypeName(), Any::class.asTypeName()))
            .addStatement("val fieldMap = mapOf($statement)", *args.toTypedArray())
            .beginControlFlow("for (entry in fieldMap) {")
            .addStatement("val fieldValue = data[entry.key]")
            .beginControlFlow("if (fieldValue != null)")
            .beginControlFlow("if (!entry.value.isInstance(fieldValue))")
            .addStatement("throw %T(\"Data has invalid data.\" + \n \"`\${entry.value}` is expected.\")", IllegalArgumentException::class)
            .endControlFlow()
            .nextControlFlow("else")
            .addStatement("throw %T(\"Data has no value with `\${entry.key}` key.\")", IllegalArgumentException::class)
            .endControlFlow()
            .endControlFlow()
            .build()
    }

    companion object {
        private const val SERIALIZER_CLASS_SUFFIX = "Serializer"
    }
}
