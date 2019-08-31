package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
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
            superclass(Types.Controller.CollectionDelete)
        }.build()

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
