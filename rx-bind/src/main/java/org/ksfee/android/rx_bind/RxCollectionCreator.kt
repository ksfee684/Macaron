package org.ksfee.android.rx_bind

import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import io.reactivex.Observable
import io.reactivex.Single
import org.ksfee.android.macaron.library.controller.CollectionCreator
import org.ksfee.android.macaron.library.model.CollectionModel
import org.ksfee.android.rx_bind.exception.TaskCancelException
import org.ksfee.android.rx_bind.exception.TaskFailureException

val <R : CollectionModel> CollectionCreator<R>.rx: RxCollectionCreator<R>
    get() = RxCollectionCreator(this)

class RxCollectionCreator<R : CollectionModel>(
    private val collectionCreator: CollectionCreator<R>
) {
    fun createAsSingle(model: R) = Single.create<R> { emitter ->
        collectionCreator.create(model)
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

    fun createAsSingle(model: R, documentPath: String) = Single.create<R> { emitter ->
        collectionCreator.create(model, documentPath)
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

    fun createAllAsObservable(models: Collection<R>) = Observable.create<R> { emitter ->
        models.forEachIndexed { i, model ->
            collectionCreator.create(model)
                .addOnSuccessListener(OnSuccessListener {
                    emitter.onNext(it)
                })
                .addOnCanceledListener(OnCanceledListener {
                    emitter.onError(TaskCancelException())
                })
                .addOnFailureListener(OnFailureListener {
                    emitter.onError(TaskFailureException())
                })
                .addOnCompleteListener(OnCompleteListener {
                    if (i == models.size - 1) {
                        emitter.onComplete()
                    }
                })
        }
    }
}
