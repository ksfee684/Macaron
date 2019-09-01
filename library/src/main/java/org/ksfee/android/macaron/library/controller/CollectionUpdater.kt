package org.ksfee.android.macaron.library.controller

import org.ksfee.android.macaron.library.model.CollectionModel

abstract class CollectionUpdater<R : CollectionModel>(
    protected val model: R
) : CollectionController<Void, R>() {
    abstract fun update(): CollectionUpdater<R>
}
