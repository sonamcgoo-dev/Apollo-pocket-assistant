package com.appolopocket.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class NotificationListenerService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    companion object {
        var instance: NotificationListenerService? = null
            private set
        
        private val _notifications = MutableSharedFlow<NotificationEvent>()
        val notifications: SharedFlow<NotificationEvent> = _notifications.asSharedFlow()
        
        private val _activeNotifications = mutableListOf<NotificationInfo>()
        val activeNotifications: List<NotificationInfo>
            get() = _activeNotifications.toList()
    }

    data class NotificationInfo(
        val key: String,
        val packageName: String,
        val title: String?,
        val text: String?,
        val subText: String?,
        val timestamp: Long,
        val isOngoing: Boolean,
        val extras: Map<String, String>
    )

    sealed class NotificationEvent {
        data class Posted(val notification: NotificationInfo) : NotificationEvent()
        data class Removed(val notification: NotificationInfo) : NotificationEvent()
        data object Updated : NotificationEvent()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        
        val info = extractNotificationInfo(sbn)
        _activeNotifications.add(info)
        
        scope.launch {
            _notifications.emit(NotificationEvent.Posted(info))
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn ?: return
        
        val info = extractNotificationInfo(sbn)
        _activeNotifications.removeIf { it.key == sbn.key }
        
        scope.launch {
            _notifications.emit(NotificationEvent.Removed(info))
        }
    }

    private fun extractNotificationInfo(sbn: StatusBarNotification): NotificationInfo {
        val extras = sbn.notification.extras
        
        return NotificationInfo(
            key = sbn.key,
            packageName = sbn.packageName,
            title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString(),
            text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString(),
            subText = extras.getCharSequence(android.app.Notification.EXTRA_SUB_TEXT)?.toString(),
            timestamp = sbn.postTime,
            isOngoing = sbn.isOngoing,
            extras = mapOf(
                "channel_id" to sbn.notification.channelId,
                "group" to (sbn.notification.group ?: ""),
                "priority" to sbn.notification.priority.toString(),
                "category" to (sbn.notification.category ?: "")
            )
        )
    }

    fun getNotificationsForPackage(packageName: String): List<NotificationInfo> {
        return _activeNotifications.filter { it.packageName == packageName }
    }

    fun getRecentNotifications(limit: Int = 20): List<NotificationInfo> {
        return _activeNotifications
            .sortedByDescending { it.timestamp }
            .take(limit)
    }

    fun clearNotificationsForPackage(packageName: String) {
        activeNotifications
            .filter { it.packageName == packageName }
            .forEach { notification ->
                try {
                    cancelNotification(notification.key)
                } catch (e: Exception) {
                    // Permission denied or notification not found
                }
            }
    }

    fun clearAllNotifications() {
        try {
            cancelAllNotifications()
        } catch (e: Exception) {
            // Permission denied
        }
    }
}
