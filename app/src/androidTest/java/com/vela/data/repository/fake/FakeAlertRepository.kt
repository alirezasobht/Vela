package com.vela.data.repository.fake

import com.vela.domain.model.Alert
import com.vela.domain.repository.AlertRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@Singleton
class FakeAlertRepository @Inject constructor() : AlertRepository {
    private val alerts = MutableStateFlow<List<Alert>>(emptyList())
    private var nextId = 1L

    override fun observeAllAlerts(): Flow<List<Alert>> = alerts

    override fun observeAlertsByCoinId(coinId: String): Flow<List<Alert>> = alerts.map { list -> list.filter { it.coinId == coinId } }

    override fun getAlertsSnapshot(coinId: String): List<Alert> = alerts.value.filter { it.coinId == coinId }

    override fun getActiveAlertsSnapshot(): List<Alert> = alerts.value.filter { !it.isTriggered }

    override suspend fun getAlertById(id: Long): Alert? = alerts.value.find { it.id == id }

    override suspend fun createAlert(alert: Alert): Long {
        val id = nextId++
        alerts.update { it + alert.copy(id = id) }
        return id
    }

    override suspend fun updateAlert(alert: Alert) {
        alerts.update { list -> list.map { if (it.id == alert.id) alert else it } }
    }

    override suspend fun deleteAlert(id: Long) {
        alerts.update { list -> list.filter { it.id != id } }
    }

    override suspend fun markTriggered(id: Long) {
        alerts.update { list -> list.map { if (it.id == id) it.copy(isTriggered = true) else it } }
    }

    override suspend fun updateOhlcCheckTimestamp(
        id: Long,
        timestamp: Long
    ) {
        alerts.update { list -> list.map { if (it.id == id) it.copy(lastOhlcCheckTimestamp = timestamp) else it } }
    }
}
