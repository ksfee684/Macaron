package org.ksfee.android.macaron.library.model

import com.google.firebase.firestore.DocumentReference
import org.ksfee.android.macaron.library.controller.CollectionDeleter

abstract class CollectionModel {
    var documentReference: DocumentReference? = null

    fun delete() = CollectionDeleter().apply {
        task = documentReference?.delete()
            ?: throw IllegalStateException("User doesn't have a document reference.")
    }
}
