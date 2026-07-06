package com.vela.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vela.domain.usecase.RunAlertChecksUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.coroutines.cancellation.CancellationException

@HiltWorker
class AlertCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val runAlertChecks: RunAlertChecksUseCase,
    private val heartbeat: AlertLoopHeartbeat
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (heartbeat.isAlive()) {
            return Result.success()
        }
        return try {
            runAlertChecks()
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
