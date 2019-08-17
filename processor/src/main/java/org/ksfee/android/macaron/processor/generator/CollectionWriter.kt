package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

class CollectionWriter(
    private val collectionModel: CollectionModel,
    private val serializer: CollectionSerializer
) {
    private val className: String = collectionModel.className + WRITER_CLASS_SUFFIX

    fun write() {
        val file = FileSpec.builder(collectionModel.packageName, className)
            .addType(
                TypeSpec.classBuilder(className)
                    .primaryConstructor(FunSpec.constructorBuilder()
                        .addParameter("reference", ClassName("com.google.firebase.firestore", "CollectionReference"), KModifier.PRIVATE)
                        .build())
                        .addProperties(buildProperties())
                        .addFunctions(listOf(
                            buildCreateFunction(),
                            buildCreateProxyFunction()
                        ))
                        .build()
            )
            .build()

        file.writeTo(collectionModel.context.outDir)
    }

    fun buildProperties(): List<PropertySpec> =
        listOf(
            PropertySpec.builder("reference", ClassName("com.google.firebase.firestore", "CollectionReference")).initializer("reference").addModifiers(KModifier.PRIVATE).build()
        )

    fun buildCreateProxyFunction(): FunSpec =
        FunSpec.builder("create").run {
            collectionModel.fields.forEach { addParameter(it.simpleName.toString(), it.javaToKotlinType()) }
            addParameter("onSuccessListener", ClassName("com.google.android.gms.tasks", "OnSuccessListener").parameterizedBy(collectionModel.type).copy(nullable = true))
            addParameter("onFailureListener", ClassName("com.google.android.gms.tasks", "OnFailureListener").copy(nullable = true))
            addParameter("onCanceledListener", ClassName("com.google.android.gms.tasks", "OnCanceledListener").copy(nullable = true))
            val fieldParams = collectionModel.fields.map { "${it.simpleName} = ${it.simpleName}" }.joinToString(", ")
            val listenerParams = "onSuccessListener = onSuccessListener, onFailureListener = onFailureListener, onCanceledListener = onCanceledListener"
            addStatement("create(%T($fieldParams), $listenerParams)", collectionModel.type)
            build()
        }

    fun buildCreateFunction(): FunSpec =
        FunSpec.builder("create").run {
            addParameter(collectionModel.className.toLowerCase(), collectionModel.type)
            addParameter("onSuccessListener", ClassName("com.google.android.gms.tasks", "OnSuccessListener").parameterizedBy(collectionModel.type).copy(nullable = true))
            addParameter("onFailureListener", ClassName("com.google.android.gms.tasks", "OnFailureListener").copy(nullable = true))
            addParameter("onCanceledListener", ClassName("com.google.android.gms.tasks", "OnCanceledListener").copy(nullable = true))
            beginControlFlow("reference.add(user).apply")
            addStatement("addOnSuccessListener { onSuccessListener?.onSuccess(${collectionModel.className.toLowerCase()}.apply{ documentReference = it }) }", serializer.className)
            addStatement("onFailureListener?.let { addOnFailureListener(it) }")
            addStatement("onCanceledListener?.let { addOnCanceledListener(it) }")
            endControlFlow()
            build()
        }

    companion object {
        private const val WRITER_CLASS_SUFFIX = "Writer"
    }
}