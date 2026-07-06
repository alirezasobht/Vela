package com.vela.data.worker

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlertLoopHeartbeat @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun update() {
        prefs.edit { putLong(KEY_HEARTBEAT, System.currentTimeMillis()) }
    }

    fun isAlive(): Boolean {
        val last = prefs.getLong(KEY_HEARTBEAT, 0L)
        return System.currentTimeMillis() - last < THRESHOLD_MS
    }

    companion object {
        private const val PREFS_NAME = "alert_loop_heartbeat"
        private const val KEY_HEARTBEAT = "last_beat"
        private const val THRESHOLD_MS = 30_000L
    }
}
