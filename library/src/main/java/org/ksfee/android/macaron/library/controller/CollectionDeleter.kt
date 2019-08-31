package org.ksfee.android.macaron.library.controller

import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task

class CollectionDeleter : CollectionController<Void, Void>() {
    override var task: Task<Void>? = null

    override fun addOnSuccessListener(onSuccessListener: OnSuccessListener<Void>) = apply {
        task?.addOnSuccessListener(onSuccessListener)
    }
}
