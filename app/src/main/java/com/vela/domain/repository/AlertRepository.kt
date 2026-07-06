package com.vela.domain.repository

import com.vela.domain.model.Alert
import kotlinx.coroutines.flow.Flow

interface AlertRepository {
    fun observeAlertsByCoinId(coinId: String): Flow<List<Alert>>

    fun observeAllAlerts(): Flow<List<Alert>>

    suspend fun getActiveAlerts(): List<Alert>

    suspend fun getAlertById(id: Long): Alert?

    suspend fun createAlert(alert: Alert): Long

    suspend fun updateAlert(alert: Alert)

    suspend fun deleteAlert(id: Long)

    suspend fun markTriggered(id: Long)

    suspend fun updateOhlcCheckTimestamp(
        id: Long,
        timestamp: Long
    )
}
