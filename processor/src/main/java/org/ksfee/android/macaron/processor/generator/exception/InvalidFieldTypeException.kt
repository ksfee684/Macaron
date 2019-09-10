package org.ksfee.android.macaron.processor.generator.exception

import org.ksfee.android.macaron.processor.generator.ext.asKotlinType
import javax.lang.model.element.VariableElement

class InvalidFieldTypeException(
    private val type: VariableElement
) : Exception() {
    override val message: String?
        get() = "${type.asKotlinType()} is not permitted in Firebase Firestore"
}
