package org.ksfee.android.rx_bind

import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import io.reactivex.Completable
import org.ksfee.android.macaron.library.model.CollectionModel
import org.ksfee.android.rx_bind.exception.TaskCancelException
import org.ksfee.android.rx_bind.exception.TaskFailureException

val CollectionModel.rx: RxCollectionModel
    get() = RxCollectionModel(this)

class RxCollectionModel(private val collectionModel: CollectionModel) {
    fun deleteAsCompletable() = Completable.create { emitter ->
        collectionModel.delete()
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
}
