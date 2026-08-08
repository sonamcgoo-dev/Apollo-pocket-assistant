package com.appolopocket

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppoloPocketApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Main Appolo Assistant channel
            val assistantChannel = NotificationChannel(
                CHANNEL_ASSISTANT,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_description)
                enableLights(true)
                lightColor = 0xFFFF00FF.toInt()
                enableVibration(true)
            }

            // LLM Service channel
            val llmChannel = NotificationChannel(
                CHANNEL_LLM_SERVICE,
                getString(R.string.llm_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.llm_channel_description)
                setShowBadge(false)
            }

            notificationManager.createNotificationChannels(listOf(assistantChannel, llmChannel))
        }
    }

    companion object {
        const val CHANNEL_ASSISTANT = "appolo_assistant"
        const val CHANNEL_LLM_SERVICE = "llm_service"
    }
}
