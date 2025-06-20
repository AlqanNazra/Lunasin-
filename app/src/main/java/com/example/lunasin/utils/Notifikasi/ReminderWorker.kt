package com.example.lunasin.utils.Notifikasi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.lunasin.R

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    companion object {
        private const val CHANNEL_ID = "reminder_channel"
    }


    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Pengingat Hutang"
        val message = inputData.getString("message") ?: "Pengingat jatuh tempo hutang."
        val angsuranKe = inputData.getInt("angsuranKe", 0)

        showNotification(title, message, angsuranKe)
        return Result.success()
    }

    private fun showNotification(title: String, message: String, angsuranKe: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pengingat Hutang",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel untuk pengingat hutang jatuh tempo"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(angsuranKe + 1000, notification)
    }
}