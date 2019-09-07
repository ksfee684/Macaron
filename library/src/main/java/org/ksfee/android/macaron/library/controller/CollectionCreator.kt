package org.ksfee.android.macaron.library.controller

import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.ksfee.android.macaron.library.model.CollectionModel

abstract class CollectionCreator<R : CollectionModel>(
    collectionPath: String
) : Controller<Void, R>() {
    protected lateinit var model: R

    private val reference: CollectionReference =
        FirebaseFirestore.getInstance().collection(collectionPath)

    private lateinit var document: DocumentReference

    override fun addOnSuccessListener(onSuccessListener: OnSuccessListener<R>) = apply {
        taskMap.forEach {
            it.value.addOnSuccessListener {
                onSuccessListener.onSuccess(model.apply { documentReference = document })
            }
        }
    }

    protected abstract fun serialize(): Map<String, Any?>

    fun create(model: R) = apply {
        enqueueTask(create(model, reference.document()))
    }

    fun create(model: R, documentPath: String) = apply {
        enqueueTask(create(model, reference.document(documentPath)))
    }

    fun createAll(models: Collection<R>) = apply {
        models.forEach { model -> enqueueTask(create(model, reference.document())) }
    }

    private fun create(model: R, document: DocumentReference): Task<Void> {
        this.document = document
        this.model = model
        return document.set(serialize())
    }
}
