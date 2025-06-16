package com.example.lunasin.utils.Notifikasi

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.lunasin.R
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "Broadcast diterima dengan action: ${intent.action}")

        if (intent.action == "com.example.lunasin.DAILY_REMINDER") {
            // Tampilkan notifikasi
            val notificationManager = NotificationManagerCompat.from(context)
            val channelId = "daily_reminder"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    channelId,
                    "Daily Reminder",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Channel untuk pengingat harian"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, channelId)
                .setContentTitle("Reminder Hutang")
                .setContentText("Cek hutangmu hari ini!")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            try {
                notificationManager.notify(1001, notification)
                Log.d("NotificationReceiver", "Notifikasi ditampilkan dengan ID 1001")
            } catch (e: SecurityException) {
                Log.e("NotificationReceiver", "Gagal menampilkan notifikasi: ${e.message}")
            }

            // Jadwalkan ulang notifikasi untuk hari berikutnya
            val hour = intent.getIntExtra("hour", 12)
            val minute = intent.getIntExtra("minute", 0)
            scheduleNextNotification(context, hour, minute)
        } else {
            Log.w("NotificationReceiver", "Action tidak dikenali: ${intent.action}")
        }
    }

    private fun scheduleNextNotification(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.lunasin.DAILY_REMINDER"
            putExtra("hour", hour)
            putExtra("minute", minute)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set waktu untuk hari berikutnya
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, 1)
        }

        // Periksa izin SCHEDULE_EXACT_ALARM
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e("NotificationReceiver", "Izin SCHEDULE_EXACT_ALARM tidak diberikan")
                return
            }
        }

        // Jadwalkan notifikasi berikutnya
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.d("NotificationReceiver", "Notifikasi dijadwalkan ulang pada ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} untuk ${calendar.time}")
        } catch (e: SecurityException) {
            Log.e("NotificationReceiver", "Gagal menjadwalkan ulang notifikasi: ${e.message}")
        }
    }
}