package com.vela.domain.scheduler

interface AlertCheckScheduler {
    fun schedule()
    fun cancel()
}
