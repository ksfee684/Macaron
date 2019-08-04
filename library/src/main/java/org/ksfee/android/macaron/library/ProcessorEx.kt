package org.ksfee.android.macaron.library

inline fun <reified T> Map<String, Any>.typedValue(key: String): T {
    this[key]?.let {
        return if (it is T) it else throw IllegalArgumentException()
    } ?: throw IllegalArgumentException()
}
