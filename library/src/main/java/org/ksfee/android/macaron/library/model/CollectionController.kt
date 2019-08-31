package org.ksfee.android.macaron.library.model

import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task

abstract class CollectionController<T, R> {

    abstract var task: Task<T>?

    abstract fun addOnSuccessListener(onSuccessListener: OnSuccessListener<R>): CollectionController<T, R>

    abstract fun addOnCanceledListener(onCanceledListener: OnCanceledListener): CollectionController<T, R>

    abstract fun addOnFailureListener(onFailureListener: OnFailureListener): CollectionController<T, R>
}
