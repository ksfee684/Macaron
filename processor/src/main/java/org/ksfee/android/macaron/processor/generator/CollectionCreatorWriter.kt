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

    fun buildCreatorType(): TypeSpec = TypeSpec.classBuilder(objectName).apply {
        // property
        addProperties(buildProperties())

        // function
        addFunction(buildCreateFunction())
        addFunction(buildCreateWithPathFunction())
    }.build()

    fun buildProperties(): List<PropertySpec> = listOf(
        PropertySpec.builder("reference", Types.CollectionReference).apply {
            initializer("%T.getInstance().collection(%S)", Types.FirestoreDatabase, model.collectionPath)
            addModifiers(KModifier.PRIVATE)
        }.build()
    )

    fun buildCreateFunction(): FunSpec = FunSpec.builder("create").apply {
        returns(Types.Task.parameterizedBy(Void::class.asTypeName()))
        addParameter(model.propertyName, model.type)
        addStatement("return reference.document().set(${model.propertyName}.toData())")
    }.build()

    fun buildCreateWithPathFunction(): FunSpec = FunSpec.builder("createWithId").apply {
        returns(Types.Task.parameterizedBy(Void::class.asTypeName()))
        addParameter(model.propertyName, model.type)
        addParameter("documentPath", String::class)
        addStatement("return reference.document(documentPath).set(${model.propertyName}.toData())")
    }.build()

    companion object {
        private const val CREATOR_CLASS_SUFFIX = "Creator"
    }
}