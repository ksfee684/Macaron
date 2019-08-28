package org.ksfee.android.macaron.processor.generator.model

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import org.ksfee.android.macaron.annotation.Collection
import org.ksfee.android.macaron.annotation.Field
import org.ksfee.android.macaron.processor.generator.GeneratorContext
import org.ksfee.android.macaron.processor.generator.ext.asKotlinType
import javax.lang.model.element.VariableElement

class CollectionModel(
    val context: GeneratorContext
) {
    val className: String = context.element.asClassName().simpleName

    val packageName: String = context.elementUtils.getPackageOf(context.element).toString()

    val propertyName: String = className.toLowerCase()

    val type: TypeName = context.element.asKotlinType()

    val collectionPath = context.element.getAnnotation(Collection::class.java).collectionPath

    val fields: List<VariableElement> =
        context.element.enclosedElements
            .filterIsInstance<VariableElement>()
            .filter { it.getAnnotation(Field::class.java) != null }
}
