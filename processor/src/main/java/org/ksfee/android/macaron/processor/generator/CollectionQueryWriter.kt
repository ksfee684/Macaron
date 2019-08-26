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

    private fun buildQueryType(): TypeSpec = TypeSpec.objectBuilder(objectName).apply {
        // property
        addProperties(buildProperties())

        // function
        addFunctions(buildWhereEqualToFuncs())
        addFunction(buildGetFunc())
        addFunction(buildDeserializeFunc())
    }.build()

    private fun buildProperties(): List<PropertySpec> =
        listOf(
            PropertySpec.builder("query", Types.FirestoreQuery.copy(nullable = true)).apply {
                mutable()
                addModifiers(KModifier.PRIVATE)
                initializer("null")
            }.build()
            ,
            PropertySpec.builder("reference", Types.CollectionReference).apply {
                initializer(
                    "%T.getInstance().collection(%S)",
                    Types.FirestoreDatabase,
                    model.collectionPath
                )
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
                    parameterArgs.add(field.simpleName)
                }
            }
            .joinToString(", ") { "${it.fieldName()} = data.%M<%T>(%S)" }

        return FunSpec.builder("deserialize").apply {
            returns(model.type)
            addParameter(
                "data",
                Map::class.asClassName().parameterizedBy(
                    String::class.asTypeName(),
                    Any::class.asTypeName()
                )
            )
            addStatement("return %T($parameters)", *parameterArgs.toTypedArray())
        }.build()
    }

    private fun buildWhereEqualToFuncs(): List<FunSpec> =
        model.fields
            .map {field ->
                FunSpec.builder("${field.simpleName}EqualTo").apply {
                    addParameter(field.simpleName.toString(), field.asKotlinType())
                    returns(ClassName(model.packageName, objectName))
                    beginControlFlow("return apply")
                    beginControlFlow("if (query != null)")
                    addStatement("query?.whereEqualTo(%S, ${field.simpleName})", field.fieldName())
                    nextControlFlow("else")
                    addStatement("query = reference.whereEqualTo(%S, ${field.simpleName})", field.fieldName())
                    endControlFlow()
                    endControlFlow()
                }.build()
            }

    private fun buildGetFunc(): FunSpec = FunSpec.builder("get").apply {
        addParameter(
            ParameterSpec.optionalBuilder(
                "onSuccessListener",
                Types.OnSuccessListener
                    .parameterizedBy(List::class.asTypeName().parameterizedBy(model.type))
                    .copy(nullable = true)
            ).build()
        )
        addParameter(
            ParameterSpec.optionalBuilder(
                "onFailureListener",
                Types.OnFailureListener.copy(nullable = true)
            ).build()
        )
        addParameter(
            ParameterSpec.optionalBuilder(
                "onCanceledListener",
                Types.OnCanceledListener.copy(nullable = true)
            ).build()
        )
        addCode("""
            query?.get()?.apply {
                addOnSuccessListener { onSuccessListener?.onSuccess(it.map { deserialize(it.data) }) }
                onFailureListener?.let { addOnFailureListener(it) }
                onCanceledListener?.let { addOnCanceledListener(it) }
            } ?: throw %T(%S)
        """.trimIndent(),
            IllegalStateException::class,
            "Query doesn't set yet."
        )
        addStatement("")
    }.build()

    companion object {
        private const val QUERY_CLASS_SUFFIX = "Query"
    }
}
