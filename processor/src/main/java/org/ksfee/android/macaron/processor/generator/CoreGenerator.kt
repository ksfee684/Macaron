package org.ksfee.android.macaron.processor.generator

class CoreGenerator(
    context: GeneratorContext
) {

    private val collectionModel = CollectionModel(context)

    fun generate() {
        val serializer = CollectionSerializer(collectionModel).apply { write() }
        CollectionQuery(collectionModel, serializer).write()
        CollectionWriter(collectionModel).write()
    }
}
