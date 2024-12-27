package com.example.talkpay

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.talkpay.ui.theme.TalkPayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TalkPayTheme {
                NotificationReaderApp()
            }
        }
    }

    private fun isNotificationListenerPermissionGranted(): Boolean {
        val componentName = ComponentName(this, NotificationListener::class.java)
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabledListeners?.contains(componentName.flattenToString()) ?: false
    }

    private fun redirectToSettings() {
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            startActivity(this)
        }
    }

    fun startTTSService(context: Context) {
        val intent = Intent(context, TTSService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopTTSService(context: Context) {
        val intent = Intent(context, TTSService::class.java)
        context.stopService(intent)
    }
}

@Composable
fun NotificationReaderApp() {
    val context = LocalContext.current
    val permissionGranted = remember { mutableStateOf(isNotificationListenerPermissionGranted(context)) }
    val latestNotification = rememberSaveable { mutableStateOf("No notifications yet.") }
    val isServiceRunning = remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current

    // Observe permission state and update UI dynamically
    LaunchedEffect(permissionGranted.value) {
        if (permissionGranted.value) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                NotificationListener.notificationDataFlow.collect { notification ->
                    Log.d("NotificationReaderApp", "Collected notification: $notification")
//                    val intent = Intent(context, TTSService::class.java)
//                    intent.putExtra("MESSAGE", "Đã nhận 1 triệu 5 vào lúc 11 giờ 10 phút")
//                    ContextCompat.startForegroundService(context, intent)

                    latestNotification.value = notification
                }
            }
        }
    }

    // Check permission when returning from settings
//    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionGranted.value = isNotificationListenerPermissionGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // UI content
    if (!permissionGranted.value) {
        PermissionRequestScreen(onGrantPermissionClick = {
            redirectToSettings(context)
        })
    } else {
        MainContent(
            latestNotification = latestNotification,
            isServiceRunning = isServiceRunning,
            onToggleClick = {
                if (isServiceRunning.value) {
                    (context as MainActivity).stopTTSService(context)
                } else {
                    (context as MainActivity).startTTSService(context)
                }
                isServiceRunning.value = !isServiceRunning.value
            }
        )
    }
}

@Composable
fun PermissionRequestScreen(onGrantPermissionClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Permission required to read notifications.", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onGrantPermissionClick) {
            Text("Grant Permission")
        }
    }
}

@Composable
fun MainContent(
    latestNotification: MutableState<String>,
    isServiceRunning: MutableState<Boolean>,
    onToggleClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Latest Notification:",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = latestNotification.value,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onToggleClick) {
            Text(if (isServiceRunning.value) "Stop Speaking" else "Start Speaking")
        }
    }
}

// Helper functions for permission checking and redirection
fun isNotificationListenerPermissionGranted(context: Context): Boolean {
    val componentName = ComponentName(context, NotificationListener::class.java)
    val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return enabledListeners?.contains(componentName.flattenToString()) ?: false
}

fun redirectToSettings(context: Context) {
    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
        context.startActivity(this)
    }
}




//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    TalkPayTheme {
//        Greeting("Android")
//    }
//}