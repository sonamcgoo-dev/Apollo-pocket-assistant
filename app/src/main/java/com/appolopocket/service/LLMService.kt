package com.appolopocket.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.appolopocket.AppoloPocketApp
import com.appolopocket.R
import com.appolopocket.ui.screens.main.MainActivity

class LLMService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.appolopocket.service.ACTION_START_LLM"
        const val ACTION_STOP = "com.appolopocket.service.ACTION_STOP_LLM"
        
        var isRunning: Boolean = false
            private set
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startForegroundService()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, AppoloPocketApp.CHANNEL_LLM_SERVICE)
            .setContentTitle("Appolo Pocket")
            .setContentText("AI Assistant Active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setColor(0xFFFF00FF.toInt())
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }
}
