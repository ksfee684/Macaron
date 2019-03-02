package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import javax.annotation.processing.ProcessingEnvironment

class CollectionSerializer(
    val collectionModel: CollectionModel,
    val outDir: File,
    val processingEnvironment: ProcessingEnvironment
) {

    private val packageName: String = collectionModel.elementUtils.getPackageOf(collectionModel.element).toString()

    private val className: String = collectionModel.element.simpleName.toString() + SERIALIZER_CLASS_SUFFIX

    fun write() {
        val file = FileSpec.builder(packageName, className)
            .addType(buildSerializer())
            .addType(buildValidator())
            .build()

        file.writeTo(outDir)
    }

    private fun buildSerializer(): TypeSpec {
        return TypeSpec.objectBuilder("${collectionModel.className}Serializer")
            .addFunction(buildSerializeFunc())
            .build()
    }

    private fun buildSerializeFunc(): FunSpec {
        val parameters = collectionModel.fields.map {
            "data.getTypedValue(\"${it.simpleName}\") as ${it.javaToKotlinType()}"
        }.joinToString(", \n")

        return FunSpec.builder("serialize")
            .returns(collectionModel.element.asClassName())
            .addParameter("data", Map::class.asClassName().parameterizedBy(String::class.asTypeName(), Any::class.asTypeName()))
            .addStatement("%N.%N(data)", buildValidator(), buildValidateMethod())
            .addStatement("return %T(", collectionModel.element.javaToKotlinType())
            .addStatement(parameters)
            .addStatement(")")
            .build()
    }

    private fun buildValidator(): TypeSpec {
        return TypeSpec.objectBuilder("${collectionModel.className}Validator")
            .addFunction(buildValidateMethod())
            .build()
    }

    private fun buildValidateMethod(): FunSpec {
        val args = mutableListOf<Any>()

        val statement = collectionModel.fields.map {
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