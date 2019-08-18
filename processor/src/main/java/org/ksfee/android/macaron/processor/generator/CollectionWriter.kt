package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.ksfee.android.macaron.processor.generator.ext.javaToKotlinType
import org.ksfee.android.macaron.processor.generator.firebase.FireStore

class CollectionWriter(
    private val model: CollectionModel
) {
    private val className: String = model.className + WRITER_CLASS_SUFFIX

    fun write() {
        FileSpec.builder(model.packageName, className).apply {
            addType(buildWriterType())
            build().writeTo(model.context.outDir)
        }
    }

    fun buildWriterType(): TypeSpec = TypeSpec.classBuilder(className).run {
        primaryConstructor(
            FunSpec.constructorBuilder().run {
                addParameter("reference", FireStore.CollectionReference, KModifier.PRIVATE)
                build()
            }
        )
        addProperties(buildProperties())
        addFunction(buildCreateFunction())
        addFunction(buildCreateProxyFunction())
        build()
    }

    fun buildProperties(): List<PropertySpec> =
        listOf(
            PropertySpec.builder("reference", FireStore.CollectionReference).run {
                initializer("reference")
                addModifiers(KModifier.PRIVATE)
                build()
            }
        )

    fun buildCreateProxyFunction(): FunSpec =
        FunSpec.builder("create").run {
            model.fields.forEach { addParameter(it.simpleName.toString(), it.javaToKotlinType()) }
            addParameter(
                "onSuccessListener",
                FireStore.OnSuccessListener.parameterizedBy(model.type).copy(nullable = true)
            )
            addParameter("onFailureListener", FireStore.OnFailureListener.copy(nullable = true))
            addParameter("onCanceledListener", FireStore.OnCanceledListener.copy(nullable = true))
            val fieldParams = model.fields.map { "${it.simpleName} = ${it.simpleName}" }.joinToString(", ")
            val listenerParams = "onSuccessListener = onSuccessListener, onFailureListener = onFailureListener, onCanceledListener = onCanceledListener"
            addStatement("create(%T($fieldParams), $listenerParams)", model.type)
            build()
        }

    fun buildCreateFunction(): FunSpec =
        FunSpec.builder("create").run {
            addParameter(model.className.toLowerCase(), model.type)
            addParameter(
                "onSuccessListener",
                FireStore.OnSuccessListener.parameterizedBy(model.type).copy(nullable = true)
            )
            addParameter("onFailureListener", FireStore.OnFailureListener.copy(nullable = true))
            addParameter("onCanceledListener", FireStore.OnCanceledListener.copy(nullable = true))
            beginControlFlow("reference.add(user).apply")
            addStatement("addOnSuccessListener { onSuccessListener?.onSuccess(${model.className.toLowerCase()}.apply{ documentReference = it }) }")
            addStatement("onFailureListener?.let { addOnFailureListener(it) }")
            addStatement("onCanceledListener?.let { addOnCanceledListener(it) }")
            endControlFlow()
            build()
        }

    companion object {
        private const val WRITER_CLASS_SUFFIX = "Writer"
    }
}