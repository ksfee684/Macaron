package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.ksfee.android.macaron.processor.generator.ext.asKotlinType
import org.ksfee.android.macaron.processor.generator.model.CollectionModel
import org.ksfee.android.macaron.processor.generator.util.Types
import javax.lang.model.element.VariableElement

class CollectionUpdaterWriter(
    private val model: CollectionModel
) : MacaronWriter(model.context) {

    private val className = model.className + UPDATER_CLASS_SUFFIX

    override fun buildFileSpec(): FileSpec.Builder =
        FileSpec.builder(model.packageName, className).addType(buildUpdaterType())

    private fun buildUpdaterType(): TypeSpec =
        TypeSpec.classBuilder(className).apply {
            // super
            superclass(Types.Controller.RxCollectionUpdater.parameterizedBy(model.type))
            addSuperclassConstructorParameter(model.propertyName)

            // constructor
            primaryConstructor(FunSpec.constructorBuilder().addModifiers(KModifier.PRIVATE).build())

            // property
            addProperty(buildProperty())

            // constructor
            primaryConstructor(buildConstructor())

            // function
            addFunction(buildOnSuccessListener())
            addFunctions(buildUpdateFieldFuncs())
            addFunction(buildUpdateObjectFunc())
            addFunction(buildUpdate())
        }.build()

    private fun buildConstructor(): FunSpec =
        FunSpec.constructorBuilder().apply {
            addParameter(model.propertyName, model.type)
        }.build()

    private fun buildProperty(): PropertySpec =
        PropertySpec.builder("objCopy", model.type).apply {
            addModifiers(KModifier.PRIVATE)
            mutable()
            initializer(model.propertyName)
        }.build()

    private fun buildUpdateFieldFuncs(): List<FunSpec> =
        model.fields.map { buildUpdateFieldFunc(it) }

    private fun buildUpdateFieldFunc(field: VariableElement): FunSpec =
        FunSpec.builder("update${field.simpleName.toString().capitalize()}").apply {
            addParameter(field.simpleName.toString(), field.asKotlinType())
            beginControlFlow("return apply")
            addStatement("objCopy = objCopy.copy(${field.simpleName} = ${field.simpleName})")
            endControlFlow()
        }.build()

    private fun buildUpdateObjectFunc(): FunSpec =
        FunSpec.builder("update").apply {
            val parameterStmt = mutableListOf<String>()
            model.fields.forEach { field ->
                parameterStmt.add("${field.simpleName} = ${field.simpleName}")
                addParameter(
                    ParameterSpec.builder(field.simpleName.toString(), field.asKotlinType()).apply {
                        defaultValue("objCopy.${field.simpleName}")
                    }.build()
                )
            }
            beginControlFlow("return run")
            addStatement("objCopy = objCopy.copy(${parameterStmt.joinToString(", ")})")
            addStatement("update()")
            endControlFlow()
        }.build()

    private fun buildUpdate(): FunSpec =
        FunSpec.builder("update").apply {
            addModifiers(KModifier.OVERRIDE)
            beginControlFlow("return apply")
            addStatement(
                "enqueueTask(model.documentReference?.update(objCopy.toData()) ?: throw %T(%S))",
                IllegalStateException::class,
                "Document doesn't have reference."
            )
            endControlFlow()
        }.build()

    private fun buildOnSuccessListener(): FunSpec =
        FunSpec.builder("addOnSuccessListener").apply {
            addParameter(
                "onSuccessListener",
                Types.Listener.OnSuccessListener.parameterizedBy(model.type)
            )
            addModifiers(KModifier.OVERRIDE)
            beginControlFlow("return apply")
            addStatement("taskMap.forEach { it.value.addOnSuccessListener { onSuccessListener.onSuccess(objCopy) } }")
            endControlFlow()
        }.build()

    companion object {
        private const val UPDATER_CLASS_SUFFIX = "Updater"
    }
}
