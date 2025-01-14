package com.example.talkpay

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

class TTSService : Service(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private val channelId = "TTS_CHANNEL"
    private val pendingMessages: Queue<String> = LinkedList()
    private var isInitialized = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        Log.d("TTSService", "Service created")
        tts = TextToSpeech(this, this)
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d("TTSService", "onStart: $utteranceId")
            }

            override fun onDone(utteranceId: String?) {
                Log.d("TTSService", "onDone: $utteranceId")
                handler.postDelayed({ processPendingMessages() }, 1000) // 1 second delay
            }

            override fun onError(utteranceId: String?) {
                Log.d("TTSService", "onError: $utteranceId")
            }
        })
        createNotificationChannel()
        startForegroundService()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("vi", "VN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTSService", "The specified language is not supported!")
                // Prompt the user to install the missing language data
                val installIntent = Intent()
                installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(installIntent)
            } else {
                Log.d("TTSService", "TextToSpeech initialized successfully in Vietnamese")
                isInitialized = true
                processPendingMessages()
            }
        } else {
            Log.e("TTSService", "TextToSpeech initialization failed")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val message = intent?.getStringExtra("MESSAGE") ?: "Không có thông báo nào"
        Log.d("TTSService", "onStartCommand received message: $message")
        speak(message)
        return START_NOT_STICKY
    }

    private fun speak(text: String) {
        pendingMessages.add(text)
        if (tts?.isSpeaking == true) {
            Log.d("TTSService", "TTS is already speaking, queuing the message: $text")
//            pendingMessages.add(text)
//            processPendingMessages()
        } else {
            Log.d("TTSService", "Speaking: $text")
//            pendingMessages.add(text)
            processPendingMessages()
        }
    }

    private fun processPendingMessages() {
        if (!isInitialized) {
            Log.d("TTSService", "TextToSpeech is not initialized yet")
            return
        }
        if (pendingMessages.isNotEmpty()) {
//            if (tts?.isSpeaking == true) {
//
//            }
            val message = pendingMessages.poll()
            val uniqueId = UUID.randomUUID().toString()
            Log.d("TTSService", "Processing pending message: $message")
            tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, uniqueId)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "TTS Background Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("TTS Service")
                .setContentText("Speaking in the background...")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("TTS Service")
                .setContentText("Speaking in the background...")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .build()
        }
        startForeground(1, notification)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
