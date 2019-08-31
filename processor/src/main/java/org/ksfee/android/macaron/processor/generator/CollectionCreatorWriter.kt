package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
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

    private fun buildCreatorType(): TypeSpec =
        TypeSpec.classBuilder(objectName).apply {
            // super
            superclass(
                Types.CollecitonController.parameterizedBy(Void::class.asTypeName(), model.type)
            )

            // property
            addProperties(buildProperties())

            // function
            addFunctions(buildListenerFuncs())
            addFunction(buildCreateFunction())
            addFunction(buildCreateWithPathFunction())
        }.build()

    private fun buildProperties(): List<PropertySpec> = listOf(
        PropertySpec.builder("reference", Types.CollectionReference).apply {
            initializer("%T.getInstance().collection(%S)", Types.FirestoreDatabase, model.collectionPath)
            addModifiers(KModifier.PRIVATE)
        }.build(),
        PropertySpec.builder("task", Types.Task.parameterizedBy(Void::class.asTypeName()).copy(nullable = true)).apply {
            addModifiers(KModifier.OVERRIDE)
            mutable()
            initializer("null")
        }.build(),
        PropertySpec.builder("document", Types.DocumentReference).apply {
            addModifiers(KModifier.LATEINIT, KModifier.PRIVATE)
            mutable()
        }.build(),
        PropertySpec.builder(model.propertyName, model.type).apply {
            addModifiers(KModifier.LATEINIT, KModifier.PRIVATE)
            mutable()
        }.build()
    )

    private fun buildListenerFuncs(): List<FunSpec> = listOf(
        FunSpec.builder("addOnSuccessListener").apply {
            addModifiers(KModifier.OVERRIDE)
            addParameter(
                "onSuccessListener",
                Types.Listener.OnSuccessListener.parameterizedBy(model.type)
            )
            beginControlFlow("return apply")
            beginControlFlow("task?.addOnSuccessListener")
            addStatement("onSuccessListener.onSuccess(${model.propertyName}.apply { documentReference = document })")
            endControlFlow()
            endControlFlow()
        }.build(),
        FunSpec.builder("addOnCanceledListener").apply {
            addModifiers(KModifier.OVERRIDE)
            addParameter("onCanceledListener", Types.Listener.OnCanceledListener)
            beginControlFlow("return apply")
            addStatement("task?.addOnCanceledListener(onCanceledListener)")
            endControlFlow()
        }.build(),
        FunSpec.builder("addOnFailureListener").apply {
            addModifiers(KModifier.OVERRIDE)
            addParameter("onFailureListener", Types.Listener.OnFailureListener)
            beginControlFlow("return apply")
            addStatement("task?.addOnFailureListener(onFailureListener)")
            endControlFlow()
        }.build()
    )

    private fun buildCreateFunction(): FunSpec =
        FunSpec.builder("create").apply {
            addParameter(model.propertyName, model.type)
            beginControlFlow("return apply")
            addStatement("document = reference.document()")
            addStatement("this.${model.propertyName} = ${model.propertyName}")
            addStatement("task = document.set(${model.propertyName}.toData())")
            endControlFlow()
        }.build()

    private fun buildCreateWithPathFunction(): FunSpec =
        FunSpec.builder("createWithId").apply {
            addParameter(model.propertyName, model.type)
            addParameter("documentPath", String::class)
            beginControlFlow("return apply")
            addStatement("document = reference.document(documentPath)")
            addStatement("this.${model.propertyName} = ${model.propertyName}")
            addStatement("task = document.set(${model.propertyName}.toData())")
            endControlFlow()
        }.build()

    companion object {
        private const val CREATOR_CLASS_SUFFIX = "Creator"
    }
}