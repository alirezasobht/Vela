package com.vela.data.source.remote.model

import com.squareup.moshi.Json

data class CategoryDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String
)
