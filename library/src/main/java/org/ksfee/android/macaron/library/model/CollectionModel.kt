package org.ksfee.android.macaron.library.model

import com.google.firebase.firestore.DocumentReference

abstract class CollectionModel {
    var documentReference: DocumentReference? = null
}
