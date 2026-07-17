package com.vela

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.vela.data.platform.AlertNotificationSender
import com.vela.data.worker.InProcessAlertCheckScheduler
import com.vela.data.worker.WorkManagerAlertCheckScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class VelaApplication :
    Application(),
    Configuration.Provider {

    @Inject
    lateinit var workManagerScheduler: WorkManagerAlertCheckScheduler

    @Inject
    lateinit var inProcessScheduler: InProcessAlertCheckScheduler

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        workManagerScheduler.schedule()
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                inProcessScheduler.schedule()
            }
        })
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            AlertNotificationSender.CHANNEL_ID,
            "Price Alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
