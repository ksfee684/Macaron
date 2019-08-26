package org.ksfee.android.macaron.processor

import com.google.auto.common.BasicAnnotationProcessor
import com.google.auto.service.AutoService
import com.google.common.collect.SetMultimap
import org.ksfee.android.macaron.annotation.Collection
import org.ksfee.android.macaron.processor.generator.*
import org.ksfee.android.macaron.processor.generator.model.CollectionModel
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

@Suppress("unused")
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
                outDir,
                processingEnv
            )
        )
    }

    companion object {
        private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}

class MacaronProcessingStep(
    private val elementUtils: Elements,
    private val outDir: File,
    private val processingEnvironment: ProcessingEnvironment
) : BasicAnnotationProcessor.ProcessingStep {

    override fun annotations() = mutableSetOf(Collection::class.java)

    override fun process(
        elementsByAnnotation: SetMultimap<Class<out Annotation>, Element>?
    ): MutableSet<Element> {
        elementsByAnnotation ?: return mutableSetOf()

        elementsByAnnotation[Collection::class.java]
            .filter { it.kind === ElementKind.CLASS }
            .filterIsInstance<TypeElement>()
            .map { GeneratorContext(it, elementUtils, processingEnvironment, outDir) }
            .map { CollectionModel(it) }
            .forEach {
                buildCreator(it)
                buildQuerie(it)
                buildUpdater(it)
                buildDeleter(it)
                buildObjectMapper(it)
            }

        return mutableSetOf()
    }

    private fun buildCreator(collectionModel: CollectionModel) {
        CollectionCreatorWriter(collectionModel).write()
    }

    private fun buildQuerie(collectionModel: CollectionModel) {
        CollectionQueryWriter(collectionModel).write()
    }

    private fun buildUpdater(collectionModel: CollectionModel) {
        CollectionUpdaterWriter(collectionModel).write()
    }

    private fun buildDeleter(collectionModel: CollectionModel) {
        CollectionDeleterWriter(collectionModel).write()
    }

    private fun buildObjectMapper(collectionModel: CollectionModel) {
        CollectionMapperWriter(collectionModel).write()
    }
}
