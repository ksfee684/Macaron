package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.*

class CollectionWriter(
    private val collectionModel: CollectionModel
) {
    private val className: String = collectionModel.className + WRITER_CLASS_SUFFIX

    fun write() {
        val file = FileSpec.builder(collectionModel.packageName, className)
            .addType(
                TypeSpec.classBuilder(className)
                        .addProperties(buildProperties())
                        .addFunctions(buildWriteFunctions())
                        .build()
            )
            .build()

        file.writeTo(collectionModel.context.outDir)
    }

    fun buildProperties(): List<PropertySpec> {
        return emptyList()
    }

    fun buildWriteFunctions(): List<FunSpec> {
        val functions = mutableListOf<FunSpec>()

        val builder = FunSpec.builder("create")



        collectionModel.fields.forEach {
            builder.addParameter(it.simpleName.toString(), it.asType().asTypeName())
        }

        functions.add(builder.build())

        return functions
    }

    companion object {
        private const val WRITER_CLASS_SUFFIX = "Writer"
    }
}