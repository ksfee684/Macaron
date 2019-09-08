package org.ksfee.android.macaron.processor.generator

import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.Elements

class GeneratorContext(
    processingEnv: ProcessingEnvironment
) {
    val outputDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION]
        ?.replace(DEFAULT_GENERATED_OUTPUT_DIR, GENERATED_OUTPUT_DIR)
        ?.let { File(it) }
        ?: throw IllegalArgumentException("Couldn't set output directory")

    val elementUtils: Elements = processingEnv.elementUtils

    companion object {
        private const val KAPT_KOTLIN_GENERATED_OPTION = "kapt.kotlin.generated"

        private const val DEFAULT_GENERATED_OUTPUT_DIR = "kaptKotlin"

        private const val GENERATED_OUTPUT_DIR = "kapt"
    }
}
