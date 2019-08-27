package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.ksfee.android.macaron.processor.generator.ext.optionalBuilder
import org.ksfee.android.macaron.processor.generator.model.CollectionModel
import org.ksfee.android.macaron.processor.generator.util.Types

class CollectionDeleterWriter(
    private val model: CollectionModel
) : MacaronWriter() {
    private val objectName: String = model.className + DELETER_CLASS_SUFFIX

    override fun write() {
        FileSpec.builder(model.packageName, objectName).apply {
            indent(DEFAULT_INDENT)
            addFunction(buildDeleteFunc())
        }.build().writeTo(model.context.outDir)
    }

    fun buildDeleteFunc(): FunSpec = FunSpec.builder("delete").apply {
        receiver(model.type)
        returns(Types.Task.parameterizedBy(Void::class.asTypeName()))
        addStatement(
            "return documentReference?.delete() ?: throw %T(%S)",
            IllegalStateException::class,
            "${model.className} doesn't have a document reference."
        )
    }.build()

    companion object {
        private const val DELETER_CLASS_SUFFIX = "Deleter"
    }
}
