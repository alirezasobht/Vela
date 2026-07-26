package com.vela.data.pricestore

import com.vela.domain.model.SimplePrice
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimplePriceStoreImplTest {
    private val store = SimplePriceStoreImpl()

    @Test
    fun `observePrice emits null for unknown id`() = runTest {
        val result = mutableListOf<SimplePrice?>()
        val job = backgroundScope.launch { store.observePrice("unknown").collect { result.add(it) } }
        runCurrent()
        assertNull(result.firstOrNull())
        job.cancel()
    }

    @Test
    fun `upsert makes price observable`() = runTest {
        val price = SimplePrice(price = 60000.0, priceChange = 1.0, marketCap = 1_000_000.0, totalVolume = 500_000.0)
        store.upsert(mapOf("bitcoin" to price))

        val result = mutableListOf<SimplePrice?>()
        val job = backgroundScope.launch { store.observePrice("bitcoin").collect { result.add(it) } }
        runCurrent()

        assertEquals(price, result.last())
        job.cancel()
    }

    @Test
    fun `partial upsert does not blank out fields from a fuller previous upsert`() = runTest {
        val full = SimplePrice(price = 60000.0, priceChange = 1.0, marketCap = 1_000_000.0, totalVolume = 500_000.0)
        store.upsert(mapOf("bitcoin" to full))

        val partial = SimplePrice(price = 61000.0, priceChange = 2.0, marketCap = null, totalVolume = null)
        store.upsert(mapOf("bitcoin" to partial))

        val result = mutableListOf<SimplePrice?>()
        val job = backgroundScope.launch { store.observePrice("bitcoin").collect { result.add(it) } }
        runCurrent()

        val latest = result.last()
        assertEquals(61000.0, latest?.price)
        assertEquals(2.0, latest?.priceChange)
        assertEquals(1_000_000.0, latest?.marketCap)
        assertEquals(500_000.0, latest?.totalVolume)
        job.cancel()
    }

    @Test
    fun `upsert for one id does not affect another`() = runTest {
        store.upsert(mapOf("bitcoin" to SimplePrice(price = 60000.0, priceChange = 1.0, marketCap = null, totalVolume = null)))
        store.upsert(mapOf("ethereum" to SimplePrice(price = 3000.0, priceChange = 0.5, marketCap = null, totalVolume = null)))

        val result = mutableListOf<SimplePrice?>()
        val job = backgroundScope.launch { store.observePrice("bitcoin").collect { result.add(it) } }
        runCurrent()

        assertEquals(60000.0, result.last()?.price)
        job.cancel()
    }
}
