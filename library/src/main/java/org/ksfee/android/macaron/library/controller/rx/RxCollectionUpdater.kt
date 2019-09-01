package org.ksfee.android.macaron.library.controller.rx

import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import io.reactivex.Single
import org.ksfee.android.macaron.library.controller.CollectionUpdater
import org.ksfee.android.macaron.library.controller.rx.exception.TaskCancelException
import org.ksfee.android.macaron.library.controller.rx.exception.TaskFailureException
import org.ksfee.android.macaron.library.model.CollectionModel

abstract class RxCollectionUpdater<R : CollectionModel>(
    model: R
) : CollectionUpdater<R>(model) {
    fun updateAsSingle() = Single.create<R> { emitter ->
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
}
