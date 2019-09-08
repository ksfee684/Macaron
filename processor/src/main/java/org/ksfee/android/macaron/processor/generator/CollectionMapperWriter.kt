package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.ksfee.android.macaron.processor.generator.ext.asKotlinType
import org.ksfee.android.macaron.processor.generator.ext.fieldName
import org.ksfee.android.macaron.processor.generator.model.CollectionModel

class CollectionMapperWriter(
    private val model: CollectionModel
) : MacaronWriter(model.context) {
            
    private val objectName: String = model.className + MAPPER_CLASS_SUFFIX

    override fun buildFileSpec(): FileSpec.Builder =
        FileSpec.builder(model.packageName, objectName).addFunction(buildObjectMapFun())

    private fun buildObjectMapFun(): FunSpec = FunSpec.builder("toData").apply {
        val dataType = Map::class.asClassName().parameterizedBy(
            String::class.asTypeName(),
            Any::class.java.asTypeName().asKotlinType().copy(nullable = true)
        ).asKotlinType()
        receiver(model.type)
        returns(dataType)
        val parameterMap =
            model.fields.joinToString(", ") { "\"${it.fieldName()}\" to ${it.simpleName}" }
        addStatement("return mapOf($parameterMap)", dataType)
    }.build()

    companion object {
        private const val MAPPER_CLASS_SUFFIX = "Mapper"
    }
}