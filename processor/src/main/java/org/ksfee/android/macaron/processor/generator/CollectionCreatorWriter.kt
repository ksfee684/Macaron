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
) : MacaronWriter(model.context) {

    private val objectName: String = model.className + CREATOR_SUFFIX

    override fun buildFileSpec(): FileSpec.Builder =
        FileSpec.builder(model.packageName, objectName).addType(buildCreatorType())

    private fun buildCreatorType(): TypeSpec =
        TypeSpec.objectBuilder(objectName).apply {
            // super
            superclass(Types.Controller.CollectionCreator.parameterizedBy(model.type))
            addSuperclassConstructorParameter("%S", model.collectionPath)

            // function
            addFunction(buildDeserialize())
        }.build()

    private fun buildDeserialize(): FunSpec =
        FunSpec.builder("serialize").apply {
            addModifiers(KModifier.OVERRIDE)
            addStatement("return model.toData()")
        }.build()

    companion object {
        private const val CREATOR_SUFFIX = "Creator"
    }
}