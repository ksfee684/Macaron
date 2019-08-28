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

    private val type = ClassName(model.packageName, className)

    override fun write() {
        FileSpec.builder(model.packageName, className).apply {
            indent(DEFAULT_INDENT)
            addType(buildQueryType())
        }.build().writeTo(model.context.outDir)
    }

    private fun buildQueryType(): TypeSpec = TypeSpec.classBuilder(className).apply {
        // property
        addProperties(buildProperties())

        // function
        addFunctions(buildWhereEqualToFuncs())
        addFunctions(buildOrderByFuncs())
        addFunction(buildLimitFunc())
        addFunction(buildGetFunc())
        addFunction(buildDeserializeFunc())
        addFunctions(buildListenerFuncs())
    }.build()

    private fun buildProperties(): List<PropertySpec> =
        listOf(
            PropertySpec.builder("reference", Types.CollectionReference).apply {
                initializer(
                    "%T.getInstance().collection(%S)",
                    Types.FirestoreDatabase,
                    model.collectionPath
                )
                addModifiers(KModifier.PRIVATE)
            }.build(),
            PropertySpec.builder("query", Types.FirestoreQuery).apply {
                mutable()
                addModifiers(KModifier.PRIVATE)
                initializer("reference")
            }.build(),
            PropertySpec.builder("task", Types.Task.parameterizedBy(Types.QuerySnapShot).copy(nullable = true)).apply {
                mutable()
                initializer("null")
                addModifiers(KModifier.PRIVATE)
            }.build()
        )

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
            addParameter("reference", Types.DocumentReference)
            addParameter(
                "data",
                Map::class.asClassName().parameterizedBy(
                    String::class.asTypeName(),
                    Any::class.asTypeName()
                )
            )
            addStatement("return %T($parameters).apply { documentReference = reference }", *parameterArgs.toTypedArray())
        }.build()
    }

    private fun buildWhereEqualToFuncs(): List<FunSpec> =
        model.fields.map {field ->
            FunSpec.builder("${field.simpleName}EqualTo").apply {
                addParameter(field.simpleName.toString(), field.asKotlinType())
                returns(type)
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
                addStatement("return apply { query.orderBy(%S, direction) }", field.fieldName())
            }.build()
        }

    private fun buildLimitFunc(): FunSpec =
        FunSpec.builder("limit").apply {
            addParameter("limit", Long::class)
            addStatement("return apply { query.limit(limit) }")
        }.build()

    private fun buildGetFunc(): FunSpec = FunSpec.builder("get").apply {
        returns(type)
        addStatement("return apply { task = query.get() }")
    }.build()

    private fun buildListenerFuncs(): List<FunSpec> = listOf(
        FunSpec.builder("addOnSuccessListener").apply {
            returns(type)
            addParameter("onSuccessListener", Types.Listener.OnSuccessListener.parameterizedBy(List::class.asTypeName().parameterizedBy(model.type)))
            addStatement("return apply { task?.addOnSuccessListener { onSuccessListener.onSuccess(it.map { deserialize(it.reference, it.data) }) } }")
        }.build(),
        FunSpec.builder("addOnCanceledListener").apply {
            returns(type)
            addParameter("onCanceledListener", Types.Listener.OnCanceledListener)
            addStatement("return apply { task?.addOnCanceledListener(onCanceledListener) }")
        }.build(),
        FunSpec.builder("addOnFailureListener").apply {
            returns(type)
            addParameter("onFailureListener", Types.Listener.OnFailureListener)
            addStatement("return apply { task?.addOnFailureListener(onFailureListener) }")
        }.build()
    )

    companion object {
        private const val QUERY_CLASS_SUFFIX = "Query"
    }
}
