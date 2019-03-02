package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import org.ksfee.android.macaron.annotation.Field
import javax.lang.model.element.VariableElement

class CollectionModel(
    val context: GeneratorContext
) {

    val fields = fields()

    val className: String = context.element.asClassName().simpleName

    val packageName: String = context.elementUtils.getPackageOf(context.element).toString()

    val type: TypeName = context.element.javaToKotlinType()

    private fun fields(): List<VariableElement> {
        val fields = mutableListOf<VariableElement>()

        context.element.enclosedElements
            .filter { it is VariableElement }
            .filter { it.getAnnotation(Field::class.java) != null }
            .map { it as VariableElement }
            .forEach { fields.add(it) }

        return fields
    }
}
