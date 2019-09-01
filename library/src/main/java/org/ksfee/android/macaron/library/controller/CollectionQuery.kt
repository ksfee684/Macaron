package org.ksfee.android.macaron.library.controller

import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.*
import org.ksfee.android.macaron.library.model.CollectionModel

abstract class CollectionQuery<R : CollectionModel>(
    collectionPath: String
) : CollectionController<QuerySnapshot, List<R>>() {
    private val reference: CollectionReference =
        FirebaseFirestore.getInstance().collection(collectionPath)

    protected var query: Query = reference

    abstract fun deserialize(documentReference: DocumentReference, data: Map<String, Any>): R

    override fun addOnSuccessListener(onSuccessListener: OnSuccessListener<List<R>>) = apply {
        taskMap.forEach {
            it.value.addOnSuccessListener { querySnapshot ->
                onSuccessListener.onSuccess(querySnapshot.map { documentSnapshot ->
                    deserialize(documentSnapshot.reference, documentSnapshot.data)
                })
            }
        }
    }

    fun limit(limit: Long) = apply { query.limit(limit) }

    fun get() = apply { enqueueTask(query.get()) }
}
