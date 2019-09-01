package org.ksfee.android.macaron.library.controller

import com.google.android.gms.tasks.OnSuccessListener

class CollectionDeleter : CollectionController<Void, Void>() {
    override fun addOnSuccessListener(onSuccessListener: OnSuccessListener<Void>) = apply {
        taskMap.forEach { it.value.addOnSuccessListener(onSuccessListener) }
    }
}
