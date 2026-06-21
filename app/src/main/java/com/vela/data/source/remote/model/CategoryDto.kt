package com.vela.data.source.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CategoryDto(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "name") val name: String
)
