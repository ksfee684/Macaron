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
    private val objectName = model.className + UPDATER_CLASS_SUFFIX

    override fun write() {
        FileSpec.builder(model.packageName, objectName).apply {
            indent(DEFAULT_INDENT)
            addType(buildUpdaterType())
        }.build().writeTo(model.context.outDir)
    }

    fun buildUpdaterType(): TypeSpec = TypeSpec.classBuilder(objectName).apply {
        val typeName = ClassName(model.packageName, objectName)

        // constructor
        primaryConstructor(buildConstructor())

        // property
        addProperties(buildProperties())

        // function
        addFunctions(buildUpdateFieldFuncs(typeName))
        addFunction(buildUpdateObjectFunc(typeName))
        addFunction(buildUpdate(typeName))
        addFunctions(buildListenerFuncs(typeName))
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

    fun buildUpdateFieldFuncs(typeName: TypeName): List<FunSpec> = model.fields.map { buildUpdateFieldFunc(typeName, it) }

    fun buildUpdateFieldFunc(typeName: TypeName, field: VariableElement): FunSpec =
        FunSpec.builder("update${field.simpleName.toString().capitalize()}").apply {
            returns(typeName)
            addParameter(field.simpleName.toString(), field.asKotlinType())
            addStatement("return apply { objCopy = objCopy.copy(${field.simpleName} = ${field.simpleName}) }")
        }.build()

    fun buildUpdateObjectFunc(typeName: TypeName): FunSpec = FunSpec.builder("update").apply {
        returns(typeName)

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

    fun buildUpdate(typeName: TypeName): FunSpec = FunSpec.builder("update").apply {
        returns(typeName)
        addStatement(
            "return apply { task = ${model.propertyName}.documentReference?.update(objCopy.toData()) ?: throw %T(%S) }",
            IllegalStateException::class,
            "Document doesn't have reference."
        )
    }.build()

    fun buildListenerFuncs(typeName: TypeName): List<FunSpec> = listOf(
        FunSpec.builder("addOnSuccessListener").apply {
            returns(typeName)
            addParameter("onSuccessListener", Types.Listener.OnSuccessListener.parameterizedBy(Void::class.asTypeName()))
            addStatement("return apply { task?.addOnSuccessListener(onSuccessListener) }")
        }.build(),
        FunSpec.builder("addOnCanceledListener").apply {
            returns(typeName)
            addParameter("onCanceledListener", Types.Listener.OnCanceledListener)
            addStatement("return apply { task?.addOnCanceledListener(onCanceledListener) }")
        }.build(),
        FunSpec.builder("addOnFailureListener").apply {
            returns(typeName)
            addParameter("onFailureListener", Types.Listener.OnFailureListener)
            addStatement("return apply { task?.addOnFailureListener(onFailureListener) }")
        }.build()
    )

    companion object {
        private const val UPDATER_CLASS_SUFFIX = "Updater"
    }
}
