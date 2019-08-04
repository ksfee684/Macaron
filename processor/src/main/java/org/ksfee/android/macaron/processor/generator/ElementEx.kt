package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.Element
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

fun Element.javaToKotlinType(recursive: Boolean = true): TypeName =
        asType().asTypeName().javaToKotlinType(recursive)

fun TypeName.javaToKotlinType(recursive: Boolean): TypeName = if (this is ParameterizedTypeName) {
    if (recursive) {
        (rawType.javaToKotlinType(recursive) as ClassName).parameterizedBy(
            *typeArguments.map { it.javaToKotlinType(recursive) }.toTypedArray()
        )
    } else {
        rawType.javaToKotlinType(recursive) as ClassName
    }
} else {
    val className = JavaToKotlinClassMap.INSTANCE
            .mapJavaToKotlin(FqName(toString()))?.asSingleFqName()?.asString()
    if (className == null) this
    else ClassName.bestGuess(className)
}
