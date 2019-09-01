package org.ksfee.android.macaron.library.controller

import com.google.android.gms.tasks.*

abstract class CollectionController<T, R> {

    protected val taskMap: MutableMap<Int, Task<T>> = mutableMapOf()

    fun enqueueTask(task: Task<T>) {
        taskMap[task.hashCode()] = task.apply { addOnCompleteListener { dequeueTask(it) } }
    }

    protected fun dequeueTask(task: Task<T>) {
        taskMap.remove(task.hashCode())
    }

    abstract fun addOnSuccessListener(onSuccessListener: OnSuccessListener<R>): CollectionController<T, R>

    fun addOnCanceledListener(onCanceledListener: OnCanceledListener) = apply {
        taskMap.forEach { it.value.addOnCanceledListener(onCanceledListener) }
    }

    fun addOnFailureListener(onFailureListener: OnFailureListener) = apply {
        taskMap.forEach { it.value.addOnFailureListener(onFailureListener) }
    }

    fun addOnCompleteListener(onCompleteListener: OnCompleteListener<T>) {
        taskMap.forEach { it.value.addOnCompleteListener(onCompleteListener) }
    }
}
