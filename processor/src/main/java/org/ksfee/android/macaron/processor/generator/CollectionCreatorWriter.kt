package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.ksfee.android.macaron.processor.generator.model.CollectionModel
import org.ksfee.android.macaron.processor.generator.util.Types

class CollectionCreatorWriter(
    private val model: CollectionModel
) : MacaronWriter(model.context) {

    private val className: String = model.className + CREATOR_CLASS_SUFFIX

    override fun buildFileSpec(): FileSpec.Builder =
        FileSpec.builder(model.packageName, className).addType(buildCreatorType())

    private fun buildCreatorType(): TypeSpec =
        TypeSpec.classBuilder(className).apply {
            // super
            superclass(Types.Controller.RxCollectionCreator.parameterizedBy(model.type))
            addSuperclassConstructorParameter("%S", model.collectionPath)

            // constructor
            primaryConstructor(FunSpec.constructorBuilder().addModifiers(KModifier.PRIVATE).build())

            // function
            addFunction(buildDeserialize())

            // companion
            addType(buildCompanionObject())
        }.build()

    private fun buildDeserialize(): FunSpec =
        FunSpec.builder("serialize").apply {
            addModifiers(KModifier.OVERRIDE)
            addStatement("return model.toData()")
        }.build()

    private fun buildCompanionObject(): TypeSpec =
        TypeSpec.companionObjectBuilder().apply {
            addFunctions(buildCreateAliases())
        }.build()

    private fun buildCreateAliases(): List<FunSpec> {
        val creatorType = ClassName(model.packageName, className)
        return listOf(
            FunSpec.builder("create").apply {
                addParameter("model", model.type)
                addStatement("return %T().create(model)", creatorType)
            }.build(),
            FunSpec.builder("create").apply {
                addParameter("model", model.type)
                addParameter("documentPath", String::class)
                addStatement("return %T().create(model, documentPath)", creatorType)
            }.build(),
            FunSpec.builder("createAll").apply {
                addParameter("models", Collection::class.asTypeName().parameterizedBy(model.type))
                addStatement("return %T().createAll(models)", creatorType)
            }.build(),
            FunSpec.builder("createAsSingle").apply {
                addParameter("model", model.type)
                addStatement("return %T().createAsSingle(model)", creatorType)
            }.build(),
            FunSpec.builder("createAsSingle").apply {
                addParameter("model", model.type)
                addParameter("documentPath", String::class)
                addStatement("return %T().createAsSingle(model, documentPath)", creatorType)
            }.build(),
            FunSpec.builder("createAllAsObservable").apply {
                addParameter("models", Collection::class.asTypeName().parameterizedBy(model.type))
                addStatement("return %T().createAllAsObservable(models)", creatorType)
            }.build()
        )
    }

    companion object {
        private const val CREATOR_CLASS_SUFFIX = "Creator"
    }
}