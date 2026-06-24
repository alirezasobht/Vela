package com.vela.data.cache

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vela.data.source.local.AppDatabase
import com.vela.data.source.local.dao.AlertDao
import com.vela.data.source.local.model.AlertEntity
import com.vela.domain.model.AlertDirection
import com.vela.domain.model.AlertType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class AlertsCacheTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: AlertDao
    private lateinit var cache: AlertsCache

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.alertDao()
        cache = AlertsCache(dao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getAlerts_emitsEmptyList_whenDatabaseIsEmpty() = runTest {
        assertEquals(emptyList<Any>(), cache.getAlerts().first())
    }

    @Test
    fun getAlerts_emitsMappedAlerts_whenDatabaseHasEntries() = runTest {
        dao.insert(alertEntity())

        val alerts = cache.getAlerts().first()
        assertEquals(1, alerts.size)
        assertEquals("bitcoin", alerts[0].coinId)
        assertEquals(AlertType.PRICE, alerts[0].type)
        assertEquals(AlertDirection.ABOVE, alerts[0].direction)
        assertEquals(80_000.0, alerts[0].targetValue, 0.0)
    }

    @Test
    fun getAlerts_emitsUpdatedList_whenDatabaseChanges() = runTest {
        assertEquals(0, cache.getAlerts().first().size)

        dao.insert(alertEntity())
        assertEquals(1, cache.getAlerts().first().size)

        dao.insert(alertEntity().copy(coinId = "ethereum", coinName = "Ethereum", coinSymbol = "eth"))
        assertEquals(2, cache.getAlerts().first().size)
    }

    @Test
    fun getSnapshot_returnsEmptyList_whenDatabaseIsEmpty() = runTest {
        assertEquals(emptyList<Any>(), cache.getSnapshot())
    }

    @Test
    fun getSnapshot_returnsCachedAlerts_afterDatabaseEmits() = runTest {
        dao.insert(alertEntity())
        // allow StateFlow to collect the DB emission
        cache.getAlerts().first()

        val snapshot = cache.getSnapshot()
        assertEquals(1, snapshot.size)
        assertEquals("bitcoin", snapshot[0].coinId)
    }

    @Test
    fun getSnapshot_reflectsLatestDatabaseState() = runTest {
        dao.insert(alertEntity())
        cache.getAlerts().first()
        assertEquals(1, cache.getSnapshot().size)

        val id = dao.insert(alertEntity().copy(coinId = "ethereum", coinName = "Ethereum", coinSymbol = "eth"))
        cache.getAlerts().first { it.size == 2 }
        assertEquals(2, cache.getSnapshot().size)

        dao.delete(id)
        cache.getAlerts().first { it.size == 1 }
        assertEquals(1, cache.getSnapshot().size)
    }

    // ----- helpers -----

    private fun alertEntity() = AlertEntity(
        coinId = "bitcoin",
        coinName = "Bitcoin",
        coinSymbol = "btc",
        type = AlertType.PRICE.name,
        direction = AlertDirection.ABOVE.name,
        targetValue = 80_000.0
    )
}
