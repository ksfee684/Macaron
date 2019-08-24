package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asTypeName
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
        addParameter(
            ParameterSpec.optionalBuilder(
                "onSuccessListener",
                Types.OnSuccessListener.parameterizedBy(Void::class.asTypeName()).copy(nullable = true)
            ).build()
        )
        addParameter(
            ParameterSpec.optionalBuilder(
                "onFailureListener",
                Types.OnFailureListener.copy(nullable = true)
            ).build()
        )
        addParameter(
            ParameterSpec.optionalBuilder(
                "onCanceledListener",
                Types.OnCanceledListener.copy(nullable = true)
            ).build()
        )
        addCode("""
            documentReference?.delete()?.apply {
                addOnSuccessListener { onSuccessListener?.onSuccess(null) }
                onFailureListener?.let { addOnFailureListener(it) }
                onCanceledListener?.let { addOnCanceledListener(it) }
            } ?: throw %T(%S)
        """.trimIndent(),
            IllegalStateException::class,
            "${model.className} doesn't have a document reference."
        )
        addStatement("")
    }.build()

    companion object {
        private const val DELETER_CLASS_SUFFIX = "Deleter"
    }
}
