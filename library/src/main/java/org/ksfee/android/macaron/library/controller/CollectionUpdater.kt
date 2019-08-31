package org.ksfee.android.macaron.library.controller

import com.google.android.gms.tasks.Task
import org.ksfee.android.macaron.library.model.CollectionModel

abstract class CollectionUpdater<R : CollectionModel>(
    protected val model: R
) : CollectionController<Void, R>() {
    override var task: Task<Void>? = null

    abstract fun update(): CollectionUpdater<R>
}
