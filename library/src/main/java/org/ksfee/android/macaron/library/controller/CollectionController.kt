package org.ksfee.android.macaron.library.controller

import com.google.android.gms.tasks.*

abstract class CollectionController<T, R> {

    abstract var task: Task<T>?

    abstract fun addOnSuccessListener(onSuccessListener: OnSuccessListener<R>): CollectionController<T, R>

    fun addOnCanceledListener(onCanceledListener: OnCanceledListener) = apply {
        task?.addOnCanceledListener(onCanceledListener)
    }

    fun addOnFailureListener(onFailureListener: OnFailureListener) = apply {
        task?.addOnFailureListener(onFailureListener)
    }

    fun addOnCompleteListener(onCompleteListener: OnCompleteListener<T>) {
        task?.addOnCompleteListener(onCompleteListener)
    }
}
