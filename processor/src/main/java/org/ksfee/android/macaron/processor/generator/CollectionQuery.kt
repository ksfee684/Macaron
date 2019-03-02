package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

class CollectionQuery(
    val model: CollectionModel,
    val serializer: CollectionSerializer
) {

    private val className: String = model.className + QUERY_CLASS_SUFFIX

    fun write() {
        val file = FileSpec.builder(model.packageName, className)
            .addType(TypeSpec.classBuilder(className).build())
            .build()

        file.writeTo(model.context.outDir)
    }

    companion object {
        private const val QUERY_CLASS_SUFFIX = "Query"
    }
}
