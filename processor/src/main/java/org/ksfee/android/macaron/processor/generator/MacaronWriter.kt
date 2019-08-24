package org.ksfee.android.macaron.processor.generator

abstract class MacaronWriter {
    abstract fun write()

    companion object {
        const val DEFAULT_INDENT = "    "
    }
}
