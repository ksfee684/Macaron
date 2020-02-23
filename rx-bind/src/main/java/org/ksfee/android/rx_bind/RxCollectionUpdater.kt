package org.ksfee.android.rx_bind

import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import io.reactivex.Single
import org.ksfee.android.macaron.library.controller.CollectionUpdater
import org.ksfee.android.macaron.library.model.CollectionModel
import org.ksfee.android.rx_bind.exception.TaskCancelException
import org.ksfee.android.rx_bind.exception.TaskFailureException

val <R : CollectionModel> CollectionUpdater<R>.rx: RxCollectionUpdater<R>
    get() = RxCollectionUpdater(this)

class RxCollectionUpdater<R : CollectionModel>(
    private val collectionUpdater: CollectionUpdater<R>
) {
    fun updateAsSingle() = Single.create<R> { emitter ->
        collectionUpdater.update()
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
