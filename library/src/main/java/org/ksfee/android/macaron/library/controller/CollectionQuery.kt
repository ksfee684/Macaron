package org.ksfee.android.macaron.library.controller

import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import org.ksfee.android.macaron.library.model.CollectionModel

abstract class CollectionQuery<R : CollectionModel>(
    collectionPath: String
) : CollectionController<QuerySnapshot, List<R>>() {
    override var task: Task<QuerySnapshot>? = null

    protected val reference: CollectionReference =
        FirebaseFirestore.getInstance().collection(collectionPath)

    protected var query: Query = reference

    abstract fun deserialize(documentReference: DocumentReference, data: Map<String, Any>): R

    override fun addOnSuccessListener(onSuccessListener: OnSuccessListener<List<R>>) = apply {
        task?.addOnSuccessListener {
            onSuccessListener.onSuccess(it.map { querySnapshot ->
                deserialize(querySnapshot.reference, querySnapshot.data)
            })
        }
    }

    fun limit(limit: Long) = apply { query.limit(limit) }

    fun get() = apply { task = query.get() }
}
