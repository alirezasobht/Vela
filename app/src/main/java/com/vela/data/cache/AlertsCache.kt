package com.vela.data.cache

import com.vela.data.source.local.dao.AlertDao
import com.vela.data.source.local.mapper.toDomain
import com.vela.domain.model.Alert
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform

@Singleton
class AlertsCache @Inject constructor(private val dao: AlertDao) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val cachedAlerts = MutableStateFlow<List<Alert>?>(null)

    init {
        dao.observeAll()
            .map { list -> list.map { it.toDomain() } }
            .onEach { cachedAlerts.value = it }
            .launchIn(scope)
    }

    fun getAlerts(): Flow<List<Alert>> = cachedAlerts
        .transform { cachedAlerts ->
            if (cachedAlerts == null) {
                emit(loadAlertsFromDatabase())
            } else {
                emit(cachedAlerts)
            }
        }
        .distinctUntilChanged()

    private suspend fun loadAlertsFromDatabase(): List<Alert> = dao.getAll()
        .map { it.toDomain() }
}
