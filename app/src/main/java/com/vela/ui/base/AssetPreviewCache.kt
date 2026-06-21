package com.vela.ui.base

import com.vela.domain.model.Asset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetPreviewCache @Inject constructor() {
    private val assetMap = mutableMapOf<String, Asset>()

    fun put(assets: List<Asset>) {
        assets.forEach { assetMap[it.id] = it }
    }

    fun put(asset: Asset) {
        assetMap[asset.id] = asset
    }

    fun get(coinId: String): Asset? = assetMap[coinId]
}
