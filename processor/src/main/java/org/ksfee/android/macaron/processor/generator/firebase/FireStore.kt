package org.ksfee.android.macaron.processor.generator.firebase

import com.squareup.kotlinpoet.ClassName

object FireStore {
    val Query = ClassName("com.google.firebase.firestore", "Query")
    val CollectionReference = ClassName("com.google.firebase.firestore", "CollectionReference")
    val OnSuccessListener = ClassName("com.google.android.gms.tasks", "OnSuccessListener")
    val OnFailureListener = ClassName("com.google.android.gms.tasks", "OnFailureListener")
    val OnCanceledListener = ClassName("com.google.android.gms.tasks", "OnCanceledListener")
}
