package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asTypeName
import org.ksfee.android.macaron.processor.generator.ext.javaToKotlinType
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
            buildUpdateFuncs().forEach { addFunction(it) }
        }.build().writeTo(model.context.outDir)
    }

    fun buildUpdateFuncs(): List<FunSpec> = model.fields.map { buildUpdateFunc(it) }

    fun buildUpdateFunc(field: VariableElement): FunSpec =
        FunSpec.builder("update${field.simpleName.toString().capitalize()}").apply {
            receiver(model.type)
            addParameter(field.simpleName.toString(), field.javaToKotlinType())
            addParameter(
                "onSuccessListener",
                Types.OnSuccessListener.parameterizedBy(Void::class.asTypeName()).copy(nullable = true)
            )
            addParameter("onFailureListener", Types.OnFailureListener.copy(nullable = true))
            addParameter("onCanceledListener", Types.OnCanceledListener.copy(nullable = true))
            addCode("""
                documentReference?.update(%S, ${field.simpleName})?.apply {
                    addOnSuccessListener { onSuccessListener?.onSuccess(null) }
                    onFailureListener?.let { addOnFailureListener(it) }
                    onCanceledListener?.let { addOnCanceledListener(it) }
                } ?: throw %T(%S)
            """.trimIndent(),
                field.simpleName.toString(),
                IllegalStateException::class,
                "${model.className} doesn't have a document reference."
            )
            addStatement("")
        }.build()

    companion object {
        private const val UPDATER_CLASS_SUFFIX = "Updater"
    }
}
