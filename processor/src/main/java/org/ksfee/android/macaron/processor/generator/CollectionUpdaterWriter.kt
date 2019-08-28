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

    private val type = ClassName(model.packageName, className)

    override fun write() {
        FileSpec.builder(model.packageName, className).apply {
            indent(DEFAULT_INDENT)
            addType(buildUpdaterType())
        }.build().writeTo(model.context.outDir)
    }

    fun buildUpdaterType(): TypeSpec = TypeSpec.classBuilder(className).apply {
        // constructor
        primaryConstructor(buildConstructor())

        // property
        addProperties(buildProperties())

        // function
        addFunctions(buildUpdateFieldFuncs())
        addFunction(buildUpdateObjectFunc())
        addFunction(buildUpdate())
        addFunctions(buildListenerFuncs())
    }.build()

    fun buildConstructor(): FunSpec = FunSpec.constructorBuilder().apply {
        addParameter(model.propertyName, model.type)
    }.build()

    fun buildProperties(): List<PropertySpec> = listOf(
        PropertySpec.builder(model.propertyName, model.type).apply {
            initializer(model.propertyName)
            addModifiers(KModifier.PRIVATE)
        }.build(),
        PropertySpec.builder("objCopy", model.type).apply {
            initializer("${model.propertyName}.copy()")
            mutable()
        }.build(),
        PropertySpec.builder("task", Types.Task.parameterizedBy(Void::class.asTypeName()).copy(nullable = true)).apply {
            initializer("null")
            mutable()
            addModifiers(KModifier.PRIVATE)
        }.build()
    )

    fun buildUpdateFieldFuncs(): List<FunSpec> = model.fields.map { buildUpdateFieldFunc(it) }

    fun buildUpdateFieldFunc(field: VariableElement): FunSpec =
        FunSpec.builder("update${field.simpleName.toString().capitalize()}").apply {
            returns(type)
            addParameter(field.simpleName.toString(), field.asKotlinType())
            addStatement("return apply { objCopy = objCopy.copy(${field.simpleName} = ${field.simpleName}) }")
        }.build()

    fun buildUpdateObjectFunc(): FunSpec = FunSpec.builder("update").apply {
        returns(type)

        val parameterStmt = mutableListOf<String>()
        model.fields.forEach { field ->
            parameterStmt.add("${field.simpleName} = ${field.simpleName}")
            addParameter(
                ParameterSpec.builder(field.simpleName.toString(), field.asKotlinType()).apply {
                    defaultValue("objCopy.${field.simpleName}")
                }.build()
            )
        }

        addStatement("return apply { objCopy = objCopy.copy(${parameterStmt.joinToString(", ")}) }")
    }.build()

    fun buildUpdate(): FunSpec = FunSpec.builder("update").apply {
        returns(type)
        addStatement(
            "return apply { task = ${model.propertyName}.documentReference?.update(objCopy.toData()) ?: throw %T(%S) }",
            IllegalStateException::class,
            "Document doesn't have reference."
        )
    }.build()

    fun buildListenerFuncs(): List<FunSpec> = listOf(
        FunSpec.builder("addOnSuccessListener").apply {
            returns(type)
            addParameter(
                "onSuccessListener",
                Types.Listener.OnSuccessListener.parameterizedBy(Void::class.asTypeName())
            )
            addStatement("return apply { task?.addOnSuccessListener(onSuccessListener) }")
        }.build(),
        FunSpec.builder("addOnCanceledListener").apply {
            returns(type)
            addParameter("onCanceledListener", Types.Listener.OnCanceledListener)
            addStatement("return apply { task?.addOnCanceledListener(onCanceledListener) }")
        }.build(),
        FunSpec.builder("addOnFailureListener").apply {
            returns(type)
            addParameter("onFailureListener", Types.Listener.OnFailureListener)
            addStatement("return apply { task?.addOnFailureListener(onFailureListener) }")
        }.build()
    )

    companion object {
        private const val UPDATER_CLASS_SUFFIX = "Updater"
    }
}
