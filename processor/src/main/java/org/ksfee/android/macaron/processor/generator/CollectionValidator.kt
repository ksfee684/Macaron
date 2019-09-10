package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.asTypeName
import org.ksfee.android.macaron.processor.generator.exception.InvalidFieldTypeException
import org.ksfee.android.macaron.processor.generator.ext.asKotlinType
import org.ksfee.android.macaron.processor.generator.model.CollectionModel
import java.util.*
import javax.lang.model.element.VariableElement
import kotlin.reflect.KClass

object CollectionValidator {

    private val VALID_TYPES: List<KClass<*>> = listOf(
        Long::class,
        Double::class,
        Boolean::class,
        Date::class,
        Map::class,
        String::class,
        ByteArray::class
    )

    @Throws(Exception::class)
    fun validate(model: CollectionModel) {
        model.fields.forEach { field ->
            validateFieldType(field)
        }
    }

    @Throws(InvalidFieldTypeException::class)
    private fun validateFieldType(field: VariableElement) {
        VALID_TYPES.forEach {
            if (field.asKotlinType().copy(nullable = false) == it.asTypeName()) { return }
        }

        throw InvalidFieldTypeException(field)
    }
}
