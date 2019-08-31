package org.ksfee.android.rx_binding.ext

import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.ksfee.android.macaron.library.controller.CollectionCreator
import org.ksfee.android.macaron.library.controller.CollectionQuery
import org.ksfee.android.macaron.library.controller.CollectionUpdater
import org.ksfee.android.macaron.library.model.CollectionModel
import org.ksfee.android.rx_binding.exception.TaskCancelException
import org.ksfee.android.rx_binding.exception.TaskFailureException

fun <R : CollectionModel> CollectionCreator<R>.createAsSingle(
    model: R
) = Single.create<R> { emitter ->
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

fun <R : CollectionModel> CollectionCreator<R>.createAllAsObservable(
    models: Collection<R>
) = Observable.create<R> { emitter ->
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

fun <R : CollectionModel> CollectionCreator<R>.createWithIdAsSingle(
    model: R,
    documentPath: String
) = Single.create<R> { emitter ->
    createWithId(model, documentPath)
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

fun CollectionModel.deleteAsCompletable() = Completable.create { emitter ->
    delete()
        .addOnSuccessListener(OnSuccessListener {
            emitter.onComplete()
        })
        .addOnCanceledListener(OnCanceledListener {
            emitter.onError(TaskCancelException())
        })
        .addOnFailureListener(OnFailureListener {
            emitter.onError(TaskFailureException())
        })
}

fun <R : CollectionModel> CollectionQuery<R>.getAsObservable() = Observable.create<R> { emitter ->
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

fun <R : CollectionModel> CollectionQuery<R>.getAsSingle() = Single.create<List<R>> { emitter ->
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

fun <R : CollectionModel> CollectionUpdater<R>.updateAsSingle() = Single.create<R> { emitter ->
    update()
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
