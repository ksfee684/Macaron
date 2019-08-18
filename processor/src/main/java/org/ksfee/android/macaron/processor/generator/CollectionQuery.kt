package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.ksfee.android.macaron.annotation.Field
import org.ksfee.android.macaron.processor.generator.ext.javaToKotlinType
import org.ksfee.android.macaron.processor.generator.firebase.FireStore

class CollectionQuery(
    private val model: CollectionModel
) {

    private val className: String = model.className + QUERY_CLASS_SUFFIX

    fun write() {
        FileSpec.builder(model.packageName, className).apply {
            addImport("org.ksfee.android.macaron.library", "typedValue")
            addType(buildQueryType())
            build().writeTo(model.context.outDir)
        }
    }

    private fun buildQueryType(): TypeSpec = TypeSpec.classBuilder(className).run {
        // constructor
        primaryConstructor(
            FunSpec.constructorBuilder().run {
                addParameter("reference", FireStore.CollectionReference, KModifier.PRIVATE)
                build()
            }
        )

        // property
        addProperty(
            PropertySpec.builder("query", FireStore.Query, KModifier.LATEINIT, KModifier.PRIVATE).run {
                mutable()
                addModifiers(KModifier.PRIVATE)
                build()
            }
        )
        addProperty(
            PropertySpec.builder("reference", FireStore.CollectionReference).run {
                initializer("reference")
                addModifiers(KModifier.PRIVATE)
                build()
            }
        )

        // function
        addFunctions(buildWhereEqualToFuncs())
        addFunction(buildGetFunc())

        // companion object
        addType(TypeSpec.companionObjectBuilder().run {
            addFunction(buildDeserializeFunc())
            build()
        })

        build()
    }

    private fun buildDeserializeFunc(): FunSpec {
        val parameters = model.fields.joinToString(", \n") {
            "data.typedValue(\"${it.simpleName}\") as ${it.javaToKotlinType()}"
        }

        return FunSpec.builder("deserialize").run {
            returns(model.type)
            addParameter(
                "data",
                Map::class.asClassName().parameterizedBy(String::class.asTypeName(), Any::class.asTypeName())
            )
            addStatement("return %T($parameters)", model.type)
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
                    .addStatement("return this.apply { query = reference.whereEqualTo(\"$key\", $key)}")
                    .returns(ClassName(model.packageName, className))
                    .build()
            }

    private fun buildGetFunc(): FunSpec = FunSpec.builder("get").run {
        addParameter(
            "onSuccessListener",
            FireStore.OnSuccessListener
                .parameterizedBy(List::class.asTypeName().parameterizedBy(model.type))
                .copy(nullable = true)
        )
        addParameter("onFailureListener", FireStore.OnFailureListener.copy(nullable = true))
        addParameter("onCanceledListener", FireStore.OnCanceledListener.copy(nullable = true))
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
