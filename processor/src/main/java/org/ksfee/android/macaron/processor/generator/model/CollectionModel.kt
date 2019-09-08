package org.ksfee.android.macaron.processor.generator.model

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import org.ksfee.android.macaron.annotation.Collection
import org.ksfee.android.macaron.annotation.Field
import org.ksfee.android.macaron.processor.generator.GeneratorContext
import org.ksfee.android.macaron.processor.generator.ext.asKotlinType
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

class CollectionModel(
    val context: GeneratorContext,
    typeElement: TypeElement
) {
    val className: String = typeElement.asClassName().simpleName

    val packageName: String = context.elementUtils.getPackageOf(typeElement).toString()

    val propertyName: String = className.toLowerCase()

    val type: TypeName = typeElement.asKotlinType()

    val collectionPath = typeElement.getAnnotation(Collection::class.java).collectionPath

    val fields: List<VariableElement> =
        typeElement.enclosedElements
            .filterIsInstance<VariableElement>()
            .filter { it.getAnnotation(Field::class.java) != null }
}
