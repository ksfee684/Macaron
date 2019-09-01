package org.ksfee.android.macaron.library.controller.rx

import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import io.reactivex.Observable
import io.reactivex.Single
import org.ksfee.android.macaron.library.controller.CollectionQuery
import org.ksfee.android.macaron.library.controller.rx.exception.TaskCancelException
import org.ksfee.android.macaron.library.controller.rx.exception.TaskFailureException
import org.ksfee.android.macaron.library.model.CollectionModel

abstract class RxCollectionQuery<R : CollectionModel>(
    collectionPath: String
) : CollectionQuery<R>(collectionPath) {

    fun getAsObservable() = Observable.create<R> { emitter ->
        get()
            .addOnSuccessListener(OnSuccessListener { list ->
                list.forEach { emitter.onNext(it) }
                emitter.onComplete()
            })
            .addOnCanceledListener(OnCanceledListener {
                emitter.onError(TaskCancelException())
            })
            .addOnFailureListener(OnFailureListener {
                emitter.onError(TaskFailureException())
            })
    }

    fun getAsSingle() = Single.create<List<R>> { emitter ->
        get()
            .addOnSuccessListener(OnSuccessListener {
                emitter.onSuccess(it)
            })
            .addOnCanceledListener(OnCanceledListener {
                emitter.onError(TaskCancelException())
            })
            .addOnFailureListener(OnFailureListener {
                emitter.onError(TaskFailureException())
            })
    }
}
