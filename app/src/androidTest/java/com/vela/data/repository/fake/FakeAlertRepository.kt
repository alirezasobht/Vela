package com.vela.data.repository.fake

import com.vela.domain.model.Alert
import com.vela.domain.repository.AlertRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeAlertRepository @Inject constructor() : AlertRepository {
    override fun observeAlertsByCoinId(coinId: String): Flow<List<Alert>> = flowOf(emptyList())

    override fun observeAllAlerts(): Flow<List<Alert>> = flowOf(emptyList())

    override fun getAlertsSnapshot(coinId: String): List<Alert> = emptyList()

    override suspend fun getAlertById(id: Long): Alert? = null

    override suspend fun createAlert(alert: Alert): Long = 0L

    override suspend fun updateAlert(alert: Alert) {}

    override suspend fun deleteAlert(id: Long) {}
}
