package org.ksfee.android.macaron.sample.model

import org.ksfee.android.macaron.annotation.Collection
import org.ksfee.android.macaron.annotation.Field
import org.ksfee.android.macaron.library.model.CollectionModel

@Collection("users")
data class User(
    @Field val name: String,
    @Field val age: Long,
    @Field val description: String?,
    @Field(name = "created_at") val createdAt: Long
) : CollectionModel()
