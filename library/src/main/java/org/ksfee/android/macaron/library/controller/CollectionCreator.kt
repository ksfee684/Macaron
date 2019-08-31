package org.ksfee.android.macaron.library.controller

import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.ksfee.android.macaron.library.model.CollectionModel

abstract class CollectionCreator<R : CollectionModel>(
    collectionPath: String
) : CollectionController<Void, R>() {
    override var task: Task<Void>? = null

    protected val reference: CollectionReference =
        FirebaseFirestore.getInstance().collection(collectionPath)

    protected lateinit var document: DocumentReference

    protected lateinit var model: R

    override fun addOnSuccessListener(onSuccessListener: OnSuccessListener<R>) = apply {
        task?.addOnSuccessListener {
            onSuccessListener.onSuccess(model.apply { documentReference = document })
        }
    }

    abstract fun create(model: R): CollectionCreator<R>

    abstract fun createWithId(model: R, documentPath: String): CollectionCreator<R>
}
