package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.ksfee.android.macaron.processor.generator.ext.asKotlinType
import org.ksfee.android.macaron.processor.generator.ext.fieldName
import org.ksfee.android.macaron.processor.generator.ext.isNullable
import org.ksfee.android.macaron.processor.generator.ext.optionalBuilder
import org.ksfee.android.macaron.processor.generator.model.CollectionModel
import org.ksfee.android.macaron.processor.generator.util.Types

class CollectionQueryWriter(
    private val model: CollectionModel
) : MacaronWriter() {

    private val objectName: String = model.className + QUERY_CLASS_SUFFIX

    override fun write() {
        FileSpec.builder(model.packageName, objectName).apply {
            indent(DEFAULT_INDENT)
            addType(buildQueryType())
        }.build().writeTo(model.context.outDir)
    }

    private fun buildQueryType(): TypeSpec = TypeSpec.classBuilder(objectName).apply {
        val typeName = ClassName(model.packageName, objectName)
        // property
        addProperties(buildProperties())

        // function
        addFunctions(buildWhereEqualToFuncs())
        addFunctions(buildOrderByFuncs())
        addFunction(buildLimitFunc())
        addFunction(buildGetFunc(typeName))
        addFunction(buildDeserializeFunc())
        addFunctions(buildListenerFuncs(typeName))
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
                returns(ClassName(model.packageName, objectName))
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

    private fun buildGetFunc(typeName: TypeName): FunSpec = FunSpec.builder("get").apply {
        returns(typeName)
        addStatement("return apply { task = query.get() }")
    }.build()

    private fun buildListenerFuncs(typeName: TypeName): List<FunSpec> = listOf(
        FunSpec.builder("addOnSuccessListener").apply {
            returns(typeName)
            addParameter("onSuccessListener", Types.Listener.OnSuccessListener.parameterizedBy(List::class.asTypeName().parameterizedBy(model.type)))
            addStatement("return apply { task?.addOnSuccessListener { onSuccessListener.onSuccess(it.map { deserialize(it.reference, it.data) }) } }")
        }.build(),
        FunSpec.builder("addOnCanceledListener").apply {
            returns(typeName)
            addParameter("onCanceledListener", Types.Listener.OnCanceledListener)
            addStatement("return apply { task?.addOnCanceledListener(onCanceledListener) }")
        }.build(),
        FunSpec.builder("addOnFailureListener").apply {
            returns(typeName)
            addParameter("onFailureListener", Types.Listener.OnFailureListener)
            addStatement("return apply { task?.addOnFailureListener(onFailureListener) }")
        }.build()
    )

    companion object {
        private const val QUERY_CLASS_SUFFIX = "Query"
    }
}
