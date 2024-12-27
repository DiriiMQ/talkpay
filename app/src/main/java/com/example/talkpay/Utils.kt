package com.example.talkpay

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat

class Utils {
    fun extractMoneyAndTime(text: String): String {
        val regex = Regex("""\+([\d,]+) VND.*lúc \d{2}-\d{2}-\d{4} (\d{2}:\d{2}:\d{2})""")
        val matchResult = regex.find(text)
        if (matchResult != null) {
            Log.d("Utils", "Matched text: ${matchResult.value}")
            val money = matchResult.groupValues[1].replace(",", "").toInt() // 1500000
            val time = matchResult.groupValues[2] // 09:30:00
            val hour = time.substring(0, 2).toInt() // 9
            val minute = time.substring(3, 5).toInt() // 30
            val second = time.substring(6, 8).toInt() // 0
            val vietnameseMoney = if (money >= 1_000_000) {
                "${money / 1_000_000} triệu ${money % 1_000_000 / 1_000} nghìn"
            } else {
                "${money / 1_000} nghìn"
            }
            val vietnameseTime = "${hour} giờ ${minute} phút ${second} giây"
            val message = "Đã nhận $vietnameseMoney vào lúc $vietnameseTime"
            Log.d("Utils", "Extracted message: $message")
            return message
        }

        return "Không tìm thấy thông tin"
    }

    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}