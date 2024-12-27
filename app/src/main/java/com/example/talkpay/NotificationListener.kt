package com.example.talkpay

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class NotificationListener : NotificationListenerService() {
    private var componentName: ComponentName? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if(componentName == null) {
            componentName = ComponentName(this, this::class.java)
        }

        componentName?.let {
            requestRebind(it)
            toggleNotificationListenerService(it)
        }
        return START_REDELIVER_INTENT
    }

    private fun toggleNotificationListenerService(componentName: ComponentName) {
        val pm = packageManager
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()

        if (componentName == null) {
            componentName = ComponentName(this, this::class.java)
        }

        componentName?.let { requestRebind(it) }
    }

    companion object {
        private val _notificationDataFlow = MutableSharedFlow<String>(replay = 1)
        val notificationDataFlow: SharedFlow<String> = _notificationDataFlow
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName ?: ""
        val extras = sbn?.notification?.extras

        val title = extras?.getCharSequence("android.title").toString()
        val text = extras?.getCharSequence("android.text").toString()

        val notificationText = "App: $packageName\nTitle: $title\nText: $text"

        // Print the notification for debugging
        Log.d("NotificationListener", notificationText)

        // Emit the notification to the flow
        _notificationDataFlow.tryEmit(notificationText)

        if (packageName == "com.VCB") {
            if (Utils().isServiceRunning(this, TTSService::class.java)) {
                val intent = Intent(this, TTSService::class.java)
                intent.putExtra("MESSAGE", Utils().extractMoneyAndTime(text))
                ContextCompat.startForegroundService(this, intent)
            }
        }
    }
}