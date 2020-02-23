package org.ksfee.android.rx_bind

import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import io.reactivex.Observable
import io.reactivex.Single
import org.ksfee.android.macaron.library.controller.CollectionQuery
import org.ksfee.android.macaron.library.model.CollectionModel
import org.ksfee.android.rx_bind.exception.TaskCancelException
import org.ksfee.android.rx_bind.exception.TaskFailureException

val <R : CollectionModel> CollectionQuery<R>.rx: RxCollectionQuery<R>
    get() = RxCollectionQuery(this)

class RxCollectionQuery<R : CollectionModel>(
    private val collectionQuery: CollectionQuery<R>
) {

    fun getAsObservable() = Observable.create<R> { emitter ->
        collectionQuery.get()
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
        collectionQuery.get()
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
