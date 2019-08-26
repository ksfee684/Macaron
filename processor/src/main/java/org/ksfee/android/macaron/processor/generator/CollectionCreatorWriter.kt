package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.ksfee.android.macaron.processor.generator.ext.optionalBuilder
import org.ksfee.android.macaron.processor.generator.model.CollectionModel
import org.ksfee.android.macaron.processor.generator.util.Types

class CollectionCreatorWriter(
    private val model: CollectionModel
) : MacaronWriter() {
    private val objectName: String = model.className + CREATOR_CLASS_SUFFIX

    override fun write() {
        FileSpec.builder(model.packageName, objectName).apply {
            indent(DEFAULT_INDENT)
            addType(buildCreatorType())
        }.build().writeTo(model.context.outDir)
    }

    fun buildCreatorType(): TypeSpec = TypeSpec.objectBuilder(objectName).apply {
        // property
        addProperties(buildProperties())

        // function
        addFunction(buildCreateFunction())
    }.build()

    fun buildProperties(): List<PropertySpec> =
        listOf(
            PropertySpec.builder("reference", Types.CollectionReference).apply {
                initializer("%T.getInstance().collection(%S)", Types.FirestoreDatabase, model.collectionPath)
                addModifiers(KModifier.PRIVATE)
            }.build()
        )

    fun buildCreateFunction(): FunSpec =
        FunSpec.builder("create").apply {
            addParameter(model.className.toLowerCase(), model.type)
            addParameter(
                ParameterSpec.optionalBuilder(
                    "onSuccessListener",
                    Types.OnSuccessListener.parameterizedBy(model.type).copy(nullable = true)
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
            beginControlFlow("reference.add(user.toData()).apply")
            addStatement("addOnSuccessListener { onSuccessListener?.onSuccess(${model.propertyName}.apply{ documentReference = it }) }")
            addStatement("onFailureListener?.let { addOnFailureListener(it) }")
            addStatement("onCanceledListener?.let { addOnCanceledListener(it) }")
            endControlFlow()
        }.build()

    fun buildCreateWithIdFunction(): FunSpec = FunSpec.builder("createWithId").apply {

    }.build()

    companion object {
        private const val CREATOR_CLASS_SUFFIX = "Creator"
    }
}