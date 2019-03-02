package org.ksfee.android.macaron.processor.generator

import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

data class GeneratorContext(
    val element: TypeElement,
    val elementUtils: Elements,
    val processingEnvironment: ProcessingEnvironment,
    val outDir: File
)
