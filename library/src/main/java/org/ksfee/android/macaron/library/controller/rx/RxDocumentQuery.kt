package org.ksfee.android.macaron.library.controller.rx

import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import io.reactivex.Observable
import io.reactivex.Single
import org.ksfee.android.macaron.library.controller.DocumentQuery
import org.ksfee.android.macaron.library.controller.rx.exception.TaskCancelException
import org.ksfee.android.macaron.library.controller.rx.exception.TaskFailureException
import org.ksfee.android.macaron.library.model.CollectionModel

abstract class RxDocumentQuery<R : CollectionModel>(
    collectionPath: String
) : DocumentQuery<R>(collectionPath) {

    fun getAsObservable(documentPath: String) = Observable.create<R> { emitter ->
        get(documentPath)
            .addOnSuccessListener(OnSuccessListener {
                emitter.onNext(it)
                emitter.onComplete()
            })
            .addOnCanceledListener(OnCanceledListener {
                emitter.onError(TaskCancelException())
            })
            .addOnFailureListener(OnFailureListener {
                emitter.onError(TaskFailureException())
            })
    }

    fun getAsSingle(documentPath: String) = Single.create<R> { emitter ->
        get(documentPath)
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
