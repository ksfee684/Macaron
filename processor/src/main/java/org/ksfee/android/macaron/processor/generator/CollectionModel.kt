package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import org.ksfee.android.macaron.annotation.Field
import org.ksfee.android.macaron.processor.generator.ext.javaToKotlinType
import javax.lang.model.element.VariableElement

class CollectionModel(
    val context: GeneratorContext
) {
    val className: String = context.element.asClassName().simpleName

    val packageName: String = context.elementUtils.getPackageOf(context.element).toString()

    val type: TypeName = context.element.javaToKotlinType()

    val fields: List<VariableElement> =
        context.element.enclosedElements
            .filterIsInstance<VariableElement>()
            .filter { it.getAnnotation(Field::class.java) != null }
}
