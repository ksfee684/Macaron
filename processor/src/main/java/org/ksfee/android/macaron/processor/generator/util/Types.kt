package org.ksfee.android.macaron.processor.generator.util

import com.squareup.kotlinpoet.ClassName

object Types {
    // Firestore
    val FirestoreQuery =
        ClassName("com.google.firebase.firestore", "Query")
    val FirestoreDatabase =
        ClassName("com.google.firebase.firestore", "FirebaseFirestore")
    val CollectionReference =
        ClassName("com.google.firebase.firestore", "CollectionReference")
    val QuerySnapShot =
        ClassName("com.google.firebase.firestore", "QuerySnapshot")
    val DocumentReference =
        ClassName("com.google.firebase.firestore", "DocumentReference")
    val DocumentSnapshot =
        ClassName("com.google.firebase.firestore", "DocumentSnapshot")
    val QueryDocumentSnapshot =
        ClassName("com.google.firebase.firestore", "QueryDocumentSnapshot")
    val Task =
        ClassName("com.google.android.gms.tasks", "Task")
    val Direction =
        ClassName("com.google.firebase.firestore", "Query.Direction")

    object Listener {
        val OnSuccessListener =
            ClassName("com.google.android.gms.tasks", "OnSuccessListener")
        val OnFailureListener =
            ClassName("com.google.android.gms.tasks", "OnFailureListener")
        val OnCanceledListener: ClassName =
            ClassName("com.google.android.gms.tasks", "OnCanceledListener")
        val OnCompleteListener =
            ClassName("com.google.android.gms.tasks", "OnCompleteListener")
    }

    // library module
    object Controller {
        val CollectionCreator =
            ClassName("org.ksfee.android.macaron.library.controller", "CollectionCreator")
        val CollectionQuery =
            ClassName("org.ksfee.android.macaron.library.controller", "CollectionQuery")
        val DocumentQuery =
            ClassName("org.ksfee.android.macaron.library.controller", "DocumentQuery")
        val CollectionUpdater =
            ClassName("org.ksfee.android.macaron.library.controller", "CollectionUpdater")
    }

    object Exception {
        val DocumentNotFoundException =
            ClassName("org.ksfee.android.macaron.library.controller.exception", "DocumentNotFoundException")
    }

    val TypedValue =
        ClassName("org.ksfee.android.macaron.library.ext", "typedValue")
    val TypedNullableValue =
        ClassName("org.ksfee.android.macaron.library.ext", "typedNullableValue")
}
