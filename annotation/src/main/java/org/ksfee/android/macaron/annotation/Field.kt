package org.ksfee.android.macaron.annotation

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Field(
    val fieldName: String = ""
)
