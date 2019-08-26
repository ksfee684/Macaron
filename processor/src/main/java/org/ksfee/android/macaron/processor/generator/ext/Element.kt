package org.ksfee.android.macaron.processor.generator.ext

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.annotations.Nullable
import org.ksfee.android.macaron.annotation.Field
import javax.lang.model.element.Element
import javax.lang.model.element.VariableElement
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

fun Element.asKotlinType(): TypeName =
    asType().asTypeName().asKotlinType()

fun TypeName.asKotlinType(): TypeName = if (this is ParameterizedTypeName) {
    (rawType.asKotlinType() as ClassName).parameterizedBy(
        *typeArguments.map { it.asKotlinType() }.toTypedArray()
    )
} else {
    val className = JavaToKotlinClassMap.INSTANCE
        .mapJavaToKotlin(FqName(toString()))?.asSingleFqName()?.asString()
    if (className == null) this
    else ClassName.bestGuess(className)
}

fun VariableElement.isNullable(): Boolean {
    return try {
        checkNotNull(getAnnotation(Nullable::class.java))
        true
    } catch (e: IllegalStateException) {
        false
    }
}

fun VariableElement.fieldName(): String {
    val fieldAnnotation = getAnnotation(Field::class.java)

    try {
        checkNotNull(fieldAnnotation)
    } catch (e: IllegalStateException) {
        return simpleName.toString()
    }

    return if (fieldAnnotation.fieldName.isEmpty()) {
        simpleName.toString()
    } else {
        fieldAnnotation.fieldName
    }
}
