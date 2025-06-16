package com.example.lunasin.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.lunasin.MainActivity
import com.example.lunasin.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.lunasin.Backend.Model.Hutang
import com.example.lunasin.Backend.Model.Tempo
import kotlinx.coroutines.tasks.await
import com.example.lunasin.Backend.Model.HutangType
import com.example.lunasin.Backend.Model.StatusBayar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.work.Data

class NotifikasiUtils(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    private fun checkNearestDueDebt(context: Context, firestore: FirebaseFirestore, onResult: (String) -> Unit) {
        firestore.collection("hutang")
            .whereEqualTo("statusBayar", StatusBayar.BELUM_LUNAS.name)
            .get()
            .addOnSuccessListener { snapshot ->
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
                val currentTime = System.currentTimeMillis()
                val debts = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { data ->
                        Hutang.fromMap(data as Map<String, Any>)
                    }
                }

                // Filter hutang dengan jatuh tempo di masa depan dan urutkan berdasarkan waktu jatuh tempo
                val nearestDebt = debts
                    .filter { debt ->
                        try {
                            val dueDate = dateFormat.parse(debt.tanggalJatuhTempo)
                            dueDate?.time ?: Long.MAX_VALUE > currentTime
                        } catch (e: Exception) {
                            false
                        }
                    }
                    .minByOrNull { debt ->
                        try {
                            dateFormat.parse(debt.tanggalJatuhTempo)?.time ?: Long.MAX_VALUE
                        } catch (e: Exception) {
                            Long.MAX_VALUE
                        }
                    }

                if (nearestDebt == null) {
                    onResult("Tidak ada hutang dengan jatuh tempo terdekat.")
                } else {
                    onResult("Hutang terdekat: ${nearestDebt.namapinjaman}, jatuh tempo pada ${nearestDebt.tanggalJatuhTempo}")
                }
            }
            .addOnFailureListener { e ->
                onResult("Gagal memeriksa hutang: ${e.message}")
            }
    }

    override suspend fun doWork(): Result {
        Log.d("NotifikasiUtils", "Worker dijalankan pada ${LocalDateTime.now()}")
        return checkAndSendNotifications(applicationContext)
    }

    companion object {
        @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
        fun showNotification(context: Context, title: String, message: String, hutangId: String? = null) {
            val channelId = "default_channel_id"
            val notificationManager = NotificationManagerCompat.from(context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Informasi Notifikasi",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Channel untuk notifikasi jatuh tempo dan klaim"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                if (hutangId != null) {
                    putExtra("hutangId", hutangId)
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        }


        suspend fun checkAndSendNotifications(context: Context): Result {
            Log.d("NotifikasiUtils", "Memeriksa notifikasi manual pada ${LocalDateTime.now()}")

            val db = FirebaseFirestore.getInstance()
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                Log.e("NotifikasiUtils", "User tidak login")
                return Result.failure()
            }

            val hariIni = LocalDate.now()
            val besok = hariIni.plusDays(1)
            val lusa = hariIni.plusDays(2)

            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Log.e("NotifikasiUtils", "Izin POST_NOTIFICATIONS tidak diberikan")
                return Result.failure()
            }

            return try {
                val documents = db.collection("hutang")
                    .whereEqualTo("id_penerima", userId)
                    .get()
                    .await()

                var hasNotifications = false

                for (doc in documents) {
                    val tanggalJatuhTempo = doc.getString("tanggalJatuhTempo") ?: continue
                    val namapinjaman = doc.getString("namapinjaman") ?: "Tidak diketahui"
                    val hutangType = doc.getString("hutangType")?.let {
                        try { HutangType.valueOf(it) } catch (e: Exception) { null }
                    }

                    try {
                        val tenggatWaktu = LocalDate.parse(tanggalJatuhTempo, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        if (tenggatWaktu in listOf(hariIni, besok, lusa)) {
                            val message = "Tenggat waktu pembayaran \"$namapinjaman\" adalah $tenggatWaktu."
                            showNotification(context, "Pengingat Pembayaran", message)
                            sendFCM(context, userId, "Pengingat Pembayaran", message)
                            hasNotifications = true
                        }
                    } catch (e: Exception) {
                        Log.e("NotifikasiUtils", "Error parsing tanggalJatuhTempo: ${e.message}")
                    }

                    if (hutangType == HutangType.SERIUS) {
                        val listTempoRaw = doc.get("listTempo") as? List<Map<String, Any>> ?: emptyList()
                        val listTempo = listTempoRaw.map { Tempo.fromMap(it) }

                        for (tempo in listTempo) {
                            try {
                                val tenggatTempo = LocalDate.parse(tempo.tanggalTempo, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                if (tenggatTempo in listOf(hariIni, besok, lusa)) {
                                    val message = "Tenggat pembayaran ke-${tempo.angsuranKe} untuk \"$namapinjaman\" jatuh tempo $tenggatTempo."
                                    showNotification(context, "Pengingat Angsuran", message)
                                    sendFCM(context, userId, "Pengingat Angsuran", message)
                                    hasNotifications = true
                                }
                            } catch (e: Exception) {
                                Log.e("NotifikasiUtils", "Error parsing tanggalTempo: ${e.message}")
                            }
                        }
                    }
                }

                if (!hasNotifications) {
                    showNotification(context, "Pengingat Hutang", "Tidak ada hutang atau angsuran yang jatuh tempo dalam 3 hari ke depan.")
                }

                Result.success()
            } catch (e: Exception) {
                Log.e("NotifikasiUtils", "Terjadi error: ${e.message}")
                Result.failure()
            }
        }


        fun sendFCM(context: Context, userId: String, title: String, message: String, hutangId: String? = null) {
            try {
                val payload = JSONObject().apply {
                    put("to", "/topics/$userId")
                    put("notification", JSONObject().apply {
                        put("title", title)
                        put("body", message)
                    })
                    put("data", JSONObject().apply {
                        put("click_action", "OPEN_MAIN_ACTIVITY")
                        if (hutangId != null) {
                            put("hutangId", hutangId)
                        }
                    })
                }

                val url = URL("https://fcm.googleapis.com/fcm/send")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "key=YOUR_SERVER_KEY") // Ganti dengan FCM Server Key

                val outputWriter = OutputStreamWriter(conn.outputStream)
                outputWriter.write(payload.toString())
                outputWriter.flush()
                outputWriter.close()

                val responseCode = conn.responseCode
                Log.d("NotifikasiUtils", "FCM response code: $responseCode")
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("NotifikasiUtils", "Gagal mengirim FCM: $responseCode")
                }
            } catch (e: Exception) {
                Log.e("NotifikasiUtils", "Error mengirim FCM: ${e.message}")
            }
        }
    }

}