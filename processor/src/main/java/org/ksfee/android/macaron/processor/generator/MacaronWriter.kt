package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.FileSpec

abstract class MacaronWriter(
    private val context: GeneratorContext
) {
    abstract fun buildFileSpec(): FileSpec.Builder

    fun write() {
        writeToFile(buildFileSpec().indent(DEFAULT_INDENT).build())
    }

    private fun writeToFile(fileSpec: FileSpec) {
        fileSpec.writeTo(context.outputDir)
    }

    companion object {
        const val DEFAULT_INDENT = "    "
    }
}
