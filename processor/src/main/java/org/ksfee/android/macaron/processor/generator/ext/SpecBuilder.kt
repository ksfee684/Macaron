package org.ksfee.android.macaron.processor.generator.ext

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName

fun ParameterSpec.Companion.optionalBuilder(
    name: String,
    type: TypeName,
    vararg modifiers: KModifier
): ParameterSpec.Builder = builder(name, type, *modifiers).apply {
    defaultValue("null")
}
