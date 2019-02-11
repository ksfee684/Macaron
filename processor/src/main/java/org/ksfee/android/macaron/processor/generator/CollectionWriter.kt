package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File

class CollectionWriter(
    collectionModel: CollectionModel,
    private val outDir: File
) {

    private val packageName: String = collectionModel.elementUtils.getPackageOf(collectionModel.element).toString()

    private val className: String = collectionModel.element.simpleName.toString() + WRITER_CLASS_SUFFIX

    fun write() {
        val file = FileSpec.builder(packageName, className)
            .addType(
                TypeSpec.classBuilder(className).build()
            )
            .build()

        file.writeTo(outDir)
    }

    companion object {
        private const val WRITER_CLASS_SUFFIX = "Writer"
    }
}