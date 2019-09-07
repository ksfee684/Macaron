package org.ksfee.android.macaron.library.controller

import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.*
import org.ksfee.android.macaron.library.model.CollectionModel

abstract class CollectionQuery<R : CollectionModel>(
    collectionPath: String
) : Controller<QuerySnapshot, List<R>>() {
    private val reference: CollectionReference =
        FirebaseFirestore.getInstance().collection(collectionPath)

    protected var query: Query = reference

    abstract fun deserialize(documentSnapshot: QueryDocumentSnapshot): R

    override fun addOnSuccessListener(onSuccessListener: OnSuccessListener<List<R>>) = apply {
        taskMap.forEach {
            it.value.addOnSuccessListener { querySnapshot ->
                onSuccessListener.onSuccess(querySnapshot.map { deserialize(it) })
            }
        }
    }

    fun limit(limit: Long) = apply { query.limit(limit) }

    fun get() = apply { enqueueTask(query.get()) }
}
