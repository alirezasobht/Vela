package com.vela.data.source.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vela.data.source.local.AppDatabase
import com.vela.data.source.local.model.AlertEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: AlertDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.alertDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_and_getById_returnsEntity() = runTest {
        val id = dao.insert(entity())
        val result = dao.getById(id)
        assertEquals(id, result?.id)
        assertEquals("bitcoin", result?.coinId)
    }

    @Test
    fun getById_returnsNull_whenNotFound() = runTest {
        assertNull(dao.getById(99L))
    }

    @Test
    fun observeAll_emitsInsertedEntities() = runTest {
        dao.insert(entity())
        dao.insert(entity().copy(coinId = "ethereum", coinName = "Ethereum", coinSymbol = "eth"))
        assertEquals(2, dao.observeAll().first().size)
    }

    @Test
    fun getAll_returnsAllEntities() = runTest {
        dao.insert(entity())
        dao.insert(entity().copy(coinId = "ethereum", coinName = "Ethereum", coinSymbol = "eth"))
        assertEquals(2, dao.getAll().size)
    }

    @Test
    fun observeByCoinId_filtersCorrectly() = runTest {
        dao.insert(entity())
        dao.insert(entity().copy(coinId = "ethereum", coinName = "Ethereum", coinSymbol = "eth"))
        val result = dao.observeByCoinId("bitcoin").first()
        assertEquals(1, result.size)
        assertEquals("bitcoin", result[0].coinId)
    }

    @Test
    fun update_overwritesTargetValue() = runTest {
        val id = dao.insert(entity())
        dao.update(entity().copy(id = id, targetValue = 99_000.0))
        assertEquals(99_000.0, dao.getById(id)?.targetValue ?: 0.0, 0.0)
    }

    @Test
    fun delete_removesEntity() = runTest {
        val id = dao.insert(entity())
        dao.delete(id)
        assertNull(dao.getById(id))
    }

    // ----- markTriggered -----

    @Test
    fun markTriggered_setsIsTriggeredTrue() = runTest {
        val id = dao.insert(entity(isTriggered = false))
        dao.markTriggered(id)
        assertEquals(true, dao.getById(id)?.isTriggered)
    }

    @Test
    fun markTriggered_doesNotAffectOtherRows() = runTest {
        val id1 = dao.insert(entity(isTriggered = false))
        val id2 = dao.insert(entity().copy(coinId = "ethereum", coinName = "Ethereum", coinSymbol = "eth", isTriggered = false))
        dao.markTriggered(id1)
        assertEquals(false, dao.getById(id2)?.isTriggered)
    }

    // ----- updateOhlcCheckTimestamp -----

    @Test
    fun updateOhlcCheckTimestamp_updatesTimestamp() = runTest {
        val id = dao.insert(entity(lastOhlcCheckTimestamp = 0L))
        dao.updateOhlcCheckTimestamp(id, 9999L)
        assertEquals(9999L, dao.getById(id)?.lastOhlcCheckTimestamp)
    }

    @Test
    fun updateOhlcCheckTimestamp_doesNotAffectOtherRows() = runTest {
        val id1 = dao.insert(entity(lastOhlcCheckTimestamp = 0L))
        val id2 = dao.insert(entity().copy(coinId = "ethereum", coinName = "Ethereum", coinSymbol = "eth", lastOhlcCheckTimestamp = 0L))
        dao.updateOhlcCheckTimestamp(id1, 9999L)
        assertEquals(0L, dao.getById(id2)?.lastOhlcCheckTimestamp)
    }

    // ----- helpers -----

    private fun entity(
        isTriggered: Boolean = false,
        lastOhlcCheckTimestamp: Long = 0L,
        ohlcAnchorTimestamp: Long = 0L
    ) = AlertEntity(
        coinId = "bitcoin",
        coinName = "Bitcoin",
        coinSymbol = "btc",
        type = "PRICE",
        direction = "ABOVE",
        targetValue = 70_000.0,
        isTriggered = isTriggered,
        lastOhlcCheckTimestamp = lastOhlcCheckTimestamp,
        ohlcAnchorTimestamp = ohlcAnchorTimestamp
    )
}
