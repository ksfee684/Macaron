package org.ksfee.android.macaron.library.controller.rx

import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import io.reactivex.Observable
import io.reactivex.Single
import org.ksfee.android.macaron.library.controller.CollectionCreator
import org.ksfee.android.macaron.library.controller.rx.exception.TaskCancelException
import org.ksfee.android.macaron.library.controller.rx.exception.TaskFailureException
import org.ksfee.android.macaron.library.model.CollectionModel

abstract class RxCollectionCreator<R : CollectionModel>(
    collectionPath: String
) : CollectionCreator<R>(collectionPath) {

    fun createAsSingle(model: R) = Single.create<R> { emitter ->
        create(model)
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
        create(model, documentPath)
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
            create(model)
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
