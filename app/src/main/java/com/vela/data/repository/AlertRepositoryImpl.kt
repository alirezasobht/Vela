package com.vela.data.repository

import com.vela.data.source.local.dao.AlertDao
import com.vela.data.source.local.mapper.toDomain
import com.vela.data.source.local.mapper.toEntity
import com.vela.domain.model.Alert
import com.vela.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AlertRepositoryImpl
    @Inject
    constructor(
        private val dao: AlertDao
    ) : AlertRepository {
        override fun observeAlertsByCoinId(coinId: String): Flow<List<Alert>> = dao.observeByCoinId(coinId).map { list -> list.map { it.toDomain() } }

        override fun observeAllAlerts(): Flow<List<Alert>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

        override suspend fun getAlertById(id: Long): Alert? = dao.getById(id)?.toDomain()

        override suspend fun createAlert(alert: Alert): Long = dao.insert(alert.toEntity())

        override suspend fun updateAlert(alert: Alert) = dao.update(alert.toEntity())

        override suspend fun deleteAlert(id: Long) = dao.delete(id)
    }
