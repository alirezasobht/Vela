package com.vela.data.cache

import com.vela.data.source.local.dao.AlertDao
import com.vela.data.source.local.mapper.toDomain
import com.vela.domain.model.Alert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertsCache @Inject constructor(dao: AlertDao) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _alerts: StateFlow<List<Alert>> = dao.observeAll()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun getAlerts(): Flow<List<Alert>> = _alerts

    fun getSnapshot(): List<Alert> = _alerts.value
}
