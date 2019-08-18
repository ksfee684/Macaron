package org.ksfee.android.macaron.processor.generator

class CoreGenerator(
    context: GeneratorContext
) {
    private val collectionModel = CollectionModel(context)

    fun generate() {
        CollectionQuery(collectionModel).write()
        CollectionWriter(collectionModel).write()
    }
}
