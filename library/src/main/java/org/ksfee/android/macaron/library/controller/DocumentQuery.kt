package org.ksfee.android.macaron.library.controller

import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import org.ksfee.android.macaron.library.model.CollectionModel

abstract class DocumentQuery<R : CollectionModel>(
    collectionPath: String
) : Controller<DocumentSnapshot, R>() {
    private val reference: CollectionReference =
        FirebaseFirestore.getInstance().collection(collectionPath)

    abstract fun deserialize(documentSnapshot: DocumentSnapshot): R

    override fun addOnSuccessListener(onSuccessListener: OnSuccessListener<R>) = apply {
        taskMap.forEach {
            it.value.addOnSuccessListener { documentSnapshot ->
                onSuccessListener.onSuccess(deserialize(documentSnapshot))
            }
        }
    }

    fun get(documentPath: String) = apply { enqueueTask(reference.document(documentPath).get()) }
}
