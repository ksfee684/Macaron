package org.ksfee.android.macaron.processor

import com.google.auto.service.AutoService
import org.ksfee.android.macaron.annotation.Collection
import org.ksfee.android.macaron.processor.generator.*
import org.ksfee.android.macaron.processor.generator.model.CollectionModel
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@Suppress("unused")
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes(value = [
    "org.ksfee.android.macaron.annotation.Collection",
    "org.ksfee.android.macaron.annotation.Field"
])
class MacaronProcessor : AbstractProcessor() {

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        if (annotations.isNullOrEmpty()) return true

        try {
            val context = GeneratorContext(processingEnv)

            roundEnv?.getElementsAnnotatedWith(Collection::class.java)
                ?.let { buildCollectionControllers(it, context) }
        } catch (e: Exception) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, e.message)
        }

        return false
    }

    private fun buildCollectionControllers(types: Set<Element>, context: GeneratorContext) {
        types
            .filterIsInstance<TypeElement>()
            .map { CollectionModel(context, it) }
            .forEach { model ->
                CollectionValidator.validate(model)
                buildWriters(model)
            }
    }

    private fun buildWriters(collectionModel: CollectionModel) {
        WRITER_REGISTRY.forEach {
            it.getConstructor(CollectionModel::class.java).newInstance(collectionModel).write()
        }
    }

    companion object {
        private val WRITER_REGISTRY: List<Class<out MacaronWriter>> = listOf(
            CollectionCreatorWriter::class.java,
            CollectionMapperWriter::class.java,
            QueryWriter::class.java,
            CollectionUpdaterWriter::class.java
        )
    }
}
