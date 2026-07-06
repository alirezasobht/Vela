package com.vela.data.worker

import com.vela.data.platform.NetworkStatusChecker
import com.vela.domain.scheduler.AlertCheckScheduler
import com.vela.domain.usecase.RunAlertChecksUseCase
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class InProcessAlertCheckScheduler @Inject constructor(
    private val runAlertChecks: RunAlertChecksUseCase,
    private val networkStatusChecker: NetworkStatusChecker,
    private val heartbeat: AlertLoopHeartbeat
) : AlertCheckScheduler {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null

    override fun schedule() {
        if (job?.isActive == true) return
        job = scope.launch {
            while (true) {
                delay(INTERVAL_SEC.seconds)
                heartbeat.update()
                if (networkStatusChecker.isConnected()) {
                    runCatching { runAlertChecks() }
                }
            }
        }
    }

    override fun cancel() {
        job?.cancel()
        job = null
    }

    companion object {
        const val INTERVAL_SEC = 15L
    }
}
