package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.ksfee.android.macaron.annotation.Field
import org.ksfee.android.macaron.processor.generator.ext.javaToKotlinType
import org.ksfee.android.macaron.processor.generator.model.CollectionModel
import org.ksfee.android.macaron.processor.generator.util.Types

class CollectionQueryWriter(
    private val model: CollectionModel
) : MacaronWriter() {

    private val objectName: String = model.className + QUERY_CLASS_SUFFIX

    override fun write() {
        FileSpec.builder(model.packageName, objectName).apply {
            indent(DEFAULT_INDENT)
            addImport(Types.TypeValue.packageName, Types.TypeValue.simpleName)
            addType(buildQueryType())
            build().writeTo(model.context.outDir)
        }
    }

    private fun buildQueryType(): TypeSpec = TypeSpec.objectBuilder(objectName).run {
        // property
        addProperties(buildProperties())

        // function
        addFunctions(buildWhereEqualToFuncs())
        addFunction(buildGetFunc())
        addFunction(buildDeserializeFunc())

        build()
    }

    private fun buildProperties(): List<PropertySpec> =
        listOf(
            PropertySpec.builder("query", Types.FirestoreQuery).run {
                mutable()
                addModifiers(KModifier.PRIVATE)
                addModifiers(KModifier.LATEINIT)
                build()
            }
            ,
            PropertySpec.builder("reference", Types.CollectionReference).run {
                initializer("%T.getInstance().collection(%S)", Types.FirestoreDatabase, model.collectionPath)
                addModifiers(KModifier.PRIVATE)
                build()
            }
        )

    private fun buildDeserializeFunc(): FunSpec {
        val parameterArgs = mutableListOf<Any>(model.type)
        val parameters = model.fields
            .apply {
                forEach {
                    parameterArgs.add(
                        MemberName(Types.TypeValue.packageName, Types.TypeValue.simpleName)
                    )
                    parameterArgs.add(it.simpleName)
                    parameterArgs.add(it.javaToKotlinType())
                }
            }
            .joinToString(", ") { "${it.simpleName} = data.%M(%S) as %T" }

        return FunSpec.builder("deserialize").run {
            returns(model.type)
            addParameter(
                "data",
                Map::class.asClassName().parameterizedBy(
                    String::class.asTypeName(),
                    Any::class.asTypeName()
                )
            )
            addStatement("return %T($parameters)", *parameterArgs.toTypedArray())
            build()
        }
    }

    private fun buildWhereEqualToFuncs(): List<FunSpec> =
        model.fields
            .map {
                val field = it.getAnnotation(Field::class.java)
                val key = if (field.fieldName.isEmpty()) {
                    it.simpleName.toString()
                } else {
                    field.fieldName
                }
                FunSpec.builder("${it.simpleName}EqualTo")
                    .addParameter(key, it.javaToKotlinType())
                    .addStatement("return apply { query = reference.whereEqualTo(%S, $key)}", key)
                    .returns(ClassName(model.packageName, objectName))
                    .build()
            }

    private fun buildGetFunc(): FunSpec = FunSpec.builder("get").run {
        addParameter(
            "onSuccessListener",
            Types.OnSuccessListener
                .parameterizedBy(List::class.asTypeName().parameterizedBy(model.type))
                .copy(nullable = true)
        )
        addParameter("onFailureListener", Types.OnFailureListener.copy(nullable = true))
        addParameter("onCanceledListener", Types.OnCanceledListener.copy(nullable = true))
        beginControlFlow("query.get().apply {")
        addStatement("addOnSuccessListener { onSuccessListener?.onSuccess(it.map { deserialize(it.data) }) }")
        addStatement("onFailureListener?.let { addOnFailureListener(it) }")
        addStatement("onCanceledListener?.let { addOnCanceledListener(it) }")
        endControlFlow()
        build()
    }

    companion object {
        private const val QUERY_CLASS_SUFFIX = "Query"
    }
}
