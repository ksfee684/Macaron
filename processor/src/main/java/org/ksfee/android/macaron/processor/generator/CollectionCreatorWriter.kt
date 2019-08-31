package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
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
            superclass(Types.Controller.CollectionCreator.parameterizedBy(model.type))
            addSuperclassConstructorParameter("%S", model.collectionPath)

            // function
            addFunction(buildCreateFunction())
            addFunction(buildCreateWithPathFunction())
        }.build()

    private fun buildCreateFunction(): FunSpec =
        FunSpec.builder("create").apply {
            addModifiers(KModifier.OVERRIDE)
            addParameter("model", model.type)
            beginControlFlow("return apply")
            addStatement("document = reference.document()")
            addStatement("this.model = model")
            addStatement("task = document.set(model.toData())")
            endControlFlow()
        }.build()

    private fun buildCreateWithPathFunction(): FunSpec =
        FunSpec.builder("createWithId").apply {
            addModifiers(KModifier.OVERRIDE)
            addParameter("model", model.type)
            addParameter("documentPath", String::class)
            beginControlFlow("return apply")
            addStatement("document = reference.document(documentPath)")
            addStatement("this.model = model")
            addStatement("task = document.set(model.toData())")
            endControlFlow()
        }.build()

    companion object {
        private const val CREATOR_CLASS_SUFFIX = "Creator"
    }
}