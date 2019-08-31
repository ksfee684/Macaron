package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.ksfee.android.macaron.processor.generator.ext.asKotlinType
import org.ksfee.android.macaron.processor.generator.model.CollectionModel
import org.ksfee.android.macaron.processor.generator.util.Types
import javax.lang.model.element.VariableElement

class CollectionUpdaterWriter(
    private val model: CollectionModel
) : MacaronWriter() {

    private val className = model.className + UPDATER_CLASS_SUFFIX

    override fun write() {
        FileSpec.builder(model.packageName, className).apply {
            indent(DEFAULT_INDENT)
            addType(buildUpdaterType())
        }.build().writeTo(model.context.outDir)
    }

    private fun buildUpdaterType(): TypeSpec =
        TypeSpec.classBuilder(className).apply {
            // super
            superclass(
                Types.CollecitonController.parameterizedBy(Void::class.asTypeName(), model.type)
            )

            // constructor
            primaryConstructor(buildConstructor())

            // propertyp
            addProperties(buildProperties())

            // function
            addFunctions(buildUpdateFieldFuncs())
            addFunction(buildUpdateObjectFunc())
            addFunction(buildUpdate())
            addFunctions(buildListenerFuncs())
        }.build()

    private fun buildConstructor(): FunSpec =
        FunSpec.constructorBuilder().apply {
            addParameter(model.propertyName, model.type)
        }.build()

    private fun buildProperties(): List<PropertySpec> = listOf(
        PropertySpec.builder("task", Types.Task.parameterizedBy(Void::class.asTypeName()).copy(nullable = true)).apply {
            initializer("null")
            mutable()
            addModifiers(KModifier.OVERRIDE)
        }.build(),
        PropertySpec.builder(model.propertyName, model.type).apply {
            initializer(model.propertyName)
            addModifiers(KModifier.PRIVATE)
        }.build(),
        PropertySpec.builder("objCopy", model.type).apply {
            initializer("${model.propertyName}.copy()")
            mutable()
        }.build()
    )

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
            beginControlFlow("return apply")
            addStatement(
                "task = ${model.propertyName}.documentReference?.update(objCopy.toData()) ?: throw %T(%S)",
                IllegalStateException::class,
                "Document doesn't have reference."
            )
            endControlFlow()
        }.build()

    private fun buildListenerFuncs(): List<FunSpec> = listOf(
        FunSpec.builder("addOnSuccessListener").apply {
            addParameter(
                "onSuccessListener",
                Types.Listener.OnSuccessListener.parameterizedBy(model.type)
            )
            addModifiers(KModifier.OVERRIDE)
            beginControlFlow("return apply")
            addStatement("task?.addOnSuccessListener { onSuccessListener.onSuccess(objCopy) }")
            endControlFlow()
        }.build(),
        FunSpec.builder("addOnCanceledListener").apply {
            addParameter("onCanceledListener", Types.Listener.OnCanceledListener)
            addModifiers(KModifier.OVERRIDE)
            beginControlFlow("return apply")
            addStatement("task?.addOnCanceledListener(onCanceledListener)")
            endControlFlow()
        }.build(),
        FunSpec.builder("addOnFailureListener").apply {
            addParameter("onFailureListener", Types.Listener.OnFailureListener)
            addModifiers(KModifier.OVERRIDE)
            beginControlFlow("return apply")
            addStatement("task?.addOnFailureListener(onFailureListener)")
            endControlFlow()
        }.build()
    )

    companion object {
        private const val UPDATER_CLASS_SUFFIX = "Updater"
    }
}
