package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.ksfee.android.macaron.processor.generator.model.CollectionModel
import org.ksfee.android.macaron.processor.generator.util.Types

class CollectionDeleterWriter(
    private val model: CollectionModel
) : MacaronWriter() {
    private val className: String = model.className + DELETER_CLASS_SUFFIX

    private val type = ClassName(model.packageName, className)

    override fun write() {
        FileSpec.builder(model.packageName, className).apply {
            indent(DEFAULT_INDENT)
            addType(buildDeleterType())
            addFunction(buildDeleteFunc())
        }.build().writeTo(model.context.outDir)
    }

    private fun buildDeleterType(): TypeSpec =
        TypeSpec.classBuilder(className).apply {
            // super
            superclass(
                Types.CollecitonController.parameterizedBy(
                    Void::class.asTypeName(),
                    Void::class.asTypeName()
                )
            )

            // property
            addProperties(buildProperties())

            // function
            addFunctions(buildListenerFuncs())
        }.build()

    private fun buildProperties(): List<PropertySpec> = listOf(
        PropertySpec.builder("task", Types.Task.parameterizedBy(Void::class.asTypeName()).copy(nullable = true)).apply {
            initializer("null")
            addModifiers(KModifier.OVERRIDE)
            mutable()
        }.build()
    )

    private fun buildListenerFuncs(): List<FunSpec> = listOf(
        FunSpec.builder("addOnSuccessListener").apply {
            addParameter(
                "onSuccessListener",
                Types.Listener.OnSuccessListener.parameterizedBy(Void::class.asTypeName())
            )
            addModifiers(KModifier.OVERRIDE)
            beginControlFlow("return apply")
            addStatement("task?.addOnSuccessListener(onSuccessListener)")
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

    private fun buildDeleteFunc(): FunSpec =
        FunSpec.builder("delete").apply {
            receiver(model.type)
            beginControlFlow("return %T().apply", type)
            addStatement(
                "task = documentReference?.delete() ?: throw %T(%S)",
                IllegalStateException::class,
                "${model.className} doesn't have a document reference."
            )
            endControlFlow()
        }.build()

    companion object {
        private const val DELETER_CLASS_SUFFIX = "Deleter"
    }
}
