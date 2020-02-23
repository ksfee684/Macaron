package org.ksfee.android.rx_bind

import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import io.reactivex.Observable
import io.reactivex.Single
import org.ksfee.android.macaron.library.controller.DocumentQuery
import org.ksfee.android.macaron.library.model.CollectionModel
import org.ksfee.android.rx_bind.exception.TaskCancelException
import org.ksfee.android.rx_bind.exception.TaskFailureException

val <R : CollectionModel> DocumentQuery<R>.rx: RxDocumentQuery<R>
    get() = RxDocumentQuery(this)

class RxDocumentQuery<R : CollectionModel>(
    private val documentQuery: DocumentQuery<R>
) {
    fun getAsObservable(documentPath: String) = Observable.create<R> { emitter ->
        documentQuery.get(documentPath)
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
        documentQuery.get(documentPath)
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
