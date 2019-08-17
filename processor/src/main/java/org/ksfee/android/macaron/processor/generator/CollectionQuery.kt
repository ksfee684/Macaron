package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.ksfee.android.macaron.annotation.Field

class CollectionQuery(
    private val model: CollectionModel,
    private val serializer: CollectionSerializer
) {

    private val className: String = model.className + QUERY_CLASS_SUFFIX

    fun write() {
        val file = FileSpec.builder(model.packageName, className)

        val queryClass = TypeSpec.classBuilder(className).apply {
            addProperty(PropertySpec.builder("query", ClassName("com.google.firebase.firestore", "Query"), KModifier.LATEINIT, KModifier.PRIVATE)
                .mutable()
                .addModifiers(KModifier.PRIVATE)

                .build())
            primaryConstructor(FunSpec.constructorBuilder()
                .addParameter("reference", ClassName("com.google.firebase.firestore", "CollectionReference"), KModifier.PRIVATE)
                .build()
            )
            addProperty(PropertySpec.builder("reference", ClassName("com.google.firebase.firestore", "CollectionReference")).initializer("reference").addModifiers(KModifier.PRIVATE).build())
            addFunctions(buildEqualsFunc())
            addFunction(buildGetFunc())
        }

        file.addType(queryClass.build())

        file.build().writeTo(model.context.outDir)
    }

    fun buildEqualsFunc(): List<FunSpec> {
        return model.fields.map {
            val field = it.getAnnotation(Field::class.java)
            val key = if (field.fieldName.isEmpty()) {
                it.simpleName.toString()
            } else {
                field.fieldName
            }
            FunSpec.builder("${it.simpleName}EqualTo")
                .addParameter(key, it.javaToKotlinType())
                .addStatement("return this.apply { query = reference.whereEqualTo(\"$key\", $key)}")
                .returns(ClassName(model.packageName, className))
                .build()
        }
    }

    fun buildGetFunc(): FunSpec = FunSpec.builder("get").run {
        addParameter("onSuccessListener", ClassName("com.google.android.gms.tasks", "OnSuccessListener").parameterizedBy(List::class.asTypeName().parameterizedBy(model.type)).copy(nullable = true))
        addParameter("onFailureListener", ClassName("com.google.android.gms.tasks", "OnFailureListener").copy(nullable = true))
        addParameter("onCanceledListener", ClassName("com.google.android.gms.tasks", "OnCanceledListener").copy(nullable = true))
        beginControlFlow("query.get().apply {")
        addStatement("addOnSuccessListener { onSuccessListener?.onSuccess(it.map { %N.serialize(it.data) }) }", serializer.className)
        addStatement("onFailureListener?.let { addOnFailureListener(it) }")
        addStatement("onCanceledListener?.let { addOnCanceledListener(it) }")
        endControlFlow()
        build()
    }

    companion object {
        private const val QUERY_CLASS_SUFFIX = "Query"
    }
}
