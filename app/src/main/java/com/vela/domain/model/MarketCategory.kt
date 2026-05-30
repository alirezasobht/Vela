package com.vela.domain.model

data class MarketCategory(
    val id: String?,
    val displayName: String
) {
    companion object {
        val ALL = MarketCategory(id = null, displayName = "All")
    }
}