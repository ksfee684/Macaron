package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import org.ksfee.android.macaron.annotation.Field
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements

class CollectionModel(
    val element: TypeElement,
    val elementUtils: Elements
) {

    val fields = fields()

    val className: String = element.asClassName().simpleName

    val type: TypeName = element.javaToKotlinType()

    private fun fields(): List<VariableElement> {
        val fields = mutableListOf<VariableElement>()

        element.enclosedElements
                .filter { it is VariableElement }
                .filter { it.getAnnotation(Field::class.java) != null }
                .map { it as VariableElement }
                .forEach { fields.add(it) }

        return fields
    }
}
