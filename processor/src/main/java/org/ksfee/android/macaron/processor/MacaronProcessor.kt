package org.ksfee.android.macaron.processor

import com.google.auto.common.BasicAnnotationProcessor
import com.google.auto.service.AutoService
import com.google.common.collect.SetMultimap
import org.ksfee.android.macaron.annotation.Collection
import org.ksfee.android.macaron.processor.generator.CollectionModel
import org.ksfee.android.macaron.processor.generator.CollectionQuery
import org.ksfee.android.macaron.processor.generator.CollectionWriter
import java.io.File
import javax.annotation.processing.Processor
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

@AutoService(Processor::class)
class MacaronProcessor : BasicAnnotationProcessor() {

    override fun getSupportedSourceVersion() = SourceVersion.RELEASE_8

    override fun getSupportedOptions() = mutableSetOf(KAPT_KOTLIN_GENERATED_OPTION_NAME)

    override fun initSteps(): MutableIterable<ProcessingStep> {
        val outDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
            ?.replace("kaptKotlin", "kapt")
            ?.let { File(it) }
            ?: throw IllegalArgumentException("There is no output directory")

        return mutableListOf(
            MacaronProcessingStep(
                processingEnv.elementUtils,
                outDir
            )
        )
    }

    companion object {
        private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}

class MacaronProcessingStep(
    private val elementUtils: Elements,
    private val outDir: File
) : BasicAnnotationProcessor.ProcessingStep {

    override fun annotations() = mutableSetOf(Collection::class.java)

    override fun process(elementsByAnnotation: SetMultimap<Class<out Annotation>, Element>?): MutableSet<Element> {
        elementsByAnnotation ?: return mutableSetOf()

        elementsByAnnotation[Collection::class.java]
            .map {
                System.out.print("kind is ${it.kind}")
                it
            }
            .filter { it.kind === ElementKind.CLASS }
            .filter { it is TypeElement }
            .map { CollectionModel(it as TypeElement, elementUtils) }
            .forEach {
                CollectionWriter(it, outDir).write()
                CollectionQuery(it, outDir).write()
            }

        return mutableSetOf()
    }
}
