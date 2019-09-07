package org.ksfee.android.macaron.library.ext

inline fun <reified T> Map<String, Any>.typedValue(key: String): T {
    return this[key]?.let {
        return if (it is T) it else throw IllegalStateException("$key has different type")
    } ?: throw IllegalStateException("$key has no value")
}

inline fun <reified T> Map<String, Any>.typedNullableValue(key: String): T? {
    return this[key]?.let {
        return if (it is T) it else throw IllegalStateException("$key has different type")
    }
}
