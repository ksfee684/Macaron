package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.ksfee.android.macaron.processor.generator.ext.asKotlinType
import org.ksfee.android.macaron.processor.generator.ext.fieldName
import org.ksfee.android.macaron.processor.generator.ext.isNullable
import org.ksfee.android.macaron.processor.generator.model.CollectionModel
import org.ksfee.android.macaron.processor.generator.util.Types

class CollectionQueryWriter(
    private val model: CollectionModel
) : MacaronWriter() {

    private val className: String = model.className + QUERY_CLASS_SUFFIX

    override fun write() {
        FileSpec.builder(model.packageName, className).apply {
            indent(DEFAULT_INDENT)
            addImport(Types.FirestoreQuery.packageName, Types.FirestoreQuery.simpleName)
            addType(buildQueryType())
        }.build().writeTo(model.context.outDir)
    }

    private fun buildQueryType(): TypeSpec = TypeSpec.classBuilder(className).apply {
        // super
        superclass(
            Types.Controller.CollectionQuery.parameterizedBy(model.type)
        )
        addSuperclassConstructorParameter("%S", model.collectionPath)

        // function
        addFunctions(buildWhereEqualToFuncs())
        addFunctions(buildOrderByFuncs())
        addFunction(buildDeserializeFunc())

    }.build()

    private fun buildDeserializeFunc(): FunSpec {
        val parameterArgs = mutableListOf<Any>(model.type)
        val parameters = model.fields
            .apply {
                forEach { field ->
                    val retrieveMethod = if (field.isNullable()) {
                        MemberName(
                            Types.TypedNullableValue.packageName,
                            Types.TypedNullableValue.simpleName
                        )
                    } else {
                        MemberName(Types.TypedValue.packageName, Types.TypedValue.simpleName)
                    }
                    parameterArgs.add(retrieveMethod)
                    parameterArgs.add(field.asKotlinType())
                    parameterArgs.add(field.fieldName())
                }
            }
            .joinToString(", ") { "${it.simpleName} = data.%M<%T>(%S)" }

        return FunSpec.builder("deserialize").apply {
            returns(model.type)
            addModifiers(KModifier.OVERRIDE)
            addParameter("reference", Types.DocumentReference)
            addParameter(
                "data",
                Map::class.asClassName().parameterizedBy(
                    String::class.asTypeName(),
                    Any::class.asTypeName()
                )
            )
            addStatement(
                "return %T($parameters).apply { documentReference = reference }",
                *parameterArgs.toTypedArray()
            )
        }.build()
    }

    private fun buildWhereEqualToFuncs(): List<FunSpec> =
        model.fields.map {field ->
            FunSpec.builder("${field.simpleName}EqualTo").apply {
                addParameter(field.simpleName.toString(), field.asKotlinType())
                addStatement(
                    "return apply { query.whereEqualTo(%S, ${field.simpleName}) }",
                    field.fieldName()
                )
            }.build()
        }

    private fun buildOrderByFuncs(): List<FunSpec> =
        model.fields.map {field ->
            FunSpec.builder("orderBy${field.simpleName.toString().capitalize()}").apply {
                addParameter(
                    ParameterSpec.builder("direction", Types.Direction).apply {
                        defaultValue("Query.Direction.ASCENDING")
                    }.build()
                )
                beginControlFlow("return apply")
                addStatement("query.orderBy(%S, direction)", field.fieldName())
                endControlFlow()
            }.build()
        }

    companion object {
        private const val QUERY_CLASS_SUFFIX = "Query"
    }
}
