package com.example.lunasin.utils.Notifikasi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.lunasin.MainActivity
import com.example.lunasin.R
import com.example.lunasin.Backend.Model.Tempo
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        private const val CHANNEL_ID = "reminder_channel"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        if (remoteMessage.data.containsKey("hutangId")) {
            val title = remoteMessage.notification?.title ?: "Hutang Diklaim"
            val body = remoteMessage.notification?.body ?: "Hutang telah diklaim."
            val hutangId = remoteMessage.data["hutangId"]

            Handler(Looper.getMainLooper()).post {
                Toast.makeText(applicationContext, "$title: $body", Toast.LENGTH_LONG).show()
                showNotification(title, body, hutangId)
            }
            return
        }

        // Logika lainnya untuk pengingat angsuran
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            val tempo = Tempo.fromMap(remoteMessage.data)
            scheduleNotifications(tempo)
        }

        remoteMessage.notification?.let {
            val title = it.title ?: "Notifikasi"
            val body = it.body ?: "Ada pesan baru."

            Log.d(TAG, "Message Notification Title: $title")
            Log.d(TAG, "Message Notification Body: $body")

            Handler(Looper.getMainLooper()).post {
                Toast.makeText(applicationContext, "$title: $body", Toast.LENGTH_LONG).show()
                showNotification(title, body, null)
            }
        }
    }

    private fun scheduleNotifications(tempo: Tempo) {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val dueDate = LocalDate.parse(tempo.tanggalTempo, formatter)
            val currentDate = LocalDate.now()

            val reminderDays = listOf(3, 2, 1, 0)

            for (daysBefore in reminderDays) {
                val reminderDate = dueDate.minusDays(daysBefore.toLong())
                if (reminderDate.isAfter(currentDate) || reminderDate.isEqual(currentDate)) {
                    val delay = ChronoUnit.DAYS.between(currentDate, reminderDate).toInt()
                    val message = when (daysBefore) {
                        0 -> "Hari ini adalah jatuh tempo angsuran ke-${tempo.angsuranKe}!"
                        else -> "Pengingat: ${daysBefore} hari lagi jatuh tempo angsuran ke-${tempo.angsuranKe}!"
                    }

                    val data = Data.Builder()
                        .putString("title", "Pengingat Hutang")
                        .putString("message", message)
                        .putInt("angsuranKe", tempo.angsuranKe)
                        .build()

                    val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                        .setInitialDelay(delay.toLong(), TimeUnit.DAYS)
                        .setInputData(data)
                        .build()

                    WorkManager.getInstance(applicationContext).enqueue(workRequest)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling notifications: ${e.message}")
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
    }

    private fun showNotification(title: String, message: String, hutangId: String? = null) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pengingat Hutang",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel untuk pengingat hutang dan klaim"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (hutangId != null) {
                putExtra("hutangId", hutangId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }
}