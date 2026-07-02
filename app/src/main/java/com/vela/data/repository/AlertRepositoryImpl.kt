package com.vela.data.repository

import com.vela.data.cache.AlertsCache
import com.vela.data.source.local.dao.AlertDao
import com.vela.data.source.local.mapper.toDomain
import com.vela.data.source.local.mapper.toEntity
import com.vela.domain.model.Alert
import com.vela.domain.repository.AlertRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlertRepositoryImpl @Inject constructor(
    private val dao: AlertDao,
    private val cache: AlertsCache
) : AlertRepository {
    override fun observeAlertsByCoinId(coinId: String): Flow<List<Alert>> = cache.getAlerts().map { alerts -> alerts.filter { it.coinId == coinId } }

    override fun observeAllAlerts(): Flow<List<Alert>> = cache.getAlerts()

    override fun getAlertsSnapshot(coinId: String): List<Alert> = cache.getSnapshot().filter { it.coinId == coinId }

    override fun getActiveAlertsSnapshot(): List<Alert> = cache.getSnapshot().filter { !it.isTriggered }

    override suspend fun getAlertById(id: Long): Alert? = dao.getById(id)?.toDomain()

    override suspend fun createAlert(alert: Alert): Long = dao.insert(alert.toEntity())

    override suspend fun updateAlert(alert: Alert) = dao.update(alert.toEntity())

    override suspend fun deleteAlert(id: Long) = dao.delete(id)

    override suspend fun markTriggered(id: Long) = dao.markTriggered(id)

    override suspend fun updateOhlcCheckTimestamp(
        id: Long,
        timestamp: Long
    ) = dao.updateOhlcCheckTimestamp(id, timestamp)
}
