package com.example.lunasin.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.lunasin.Backend.model.Tempo
import java.time.format.DateTimeFormatter
import kotlin.math.log


class NotifikasiUtils(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.failure()

        val hariIni = LocalDate.now()
        val besok = hariIni.plusDays(1)
        val lusa = hariIni.plusDays(2)

        try {
            db.collection("hutang")
                .whereEqualTo("id_penerima", userId)
                .get()
                .addOnSuccessListener { documents ->
                    val hasPermission = ContextCompat.checkSelfPermission(
                        applicationContext,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        return@addOnSuccessListener // Jangan tampilkan notifikasi kalau belum diizinkan
                    }

                    for (doc in documents) {
                        val tanggal = doc.getString("tanggalBayar") ?: continue
                        val namaPinjaman = doc.getString("namapinjaman") ?: "Tidak diketahui"

                        try {
                            val tenggatWaktu = LocalDate.parse(tanggal)
                            if (tenggatWaktu == hariIni || tenggatWaktu == besok || tenggatWaktu == lusa) {
                                val namaPinjaman =
                                    doc.getString("namapinjaman") ?: "Tidak diketahui"
                                showNotification(
                                    applicationContext,
                                    "Pengingat Pembayaran",
                                    "Tenggat waktu pembayaran \"$namaPinjaman\" adalah $tenggatWaktu."
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("NotifikasiUtils", "Error parsing tanggal: ${e.message}")
                            continue
                        }

                        val listTempoRaw =
                            doc.get("listTempo") as? List<Map<String, Any>> ?: emptyList()
                        val listTempo = listTempoRaw.map { Tempo.fromMap(it) }

                        for (tempo in listTempo) {
                            try {
                                val tenggatTempo = LocalDate.parse(
                                    tempo.tanggalTempo,
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                )
                                if (tenggatTempo == hariIni || tenggatTempo == besok || tenggatTempo == lusa) {
                                    val namaPinjaman =
                                        doc.getString("namapinjaman") ?: "Tidak diketahui"
                                    showNotification(
                                        applicationContext,
                                        "Pengingat Angsuran",
                                        "Tenggat waktu pembayaran ke-${tempo.angsuranKe} untuk \"$namaPinjaman\" jatuh tempo pada $tenggatTempo."
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e("NotifikasiUtils", "Error parsing tanggal: ${e.message}")
                                continue
                            }
                        }

                    }
                }


            return Result.success()
        }catch (e: Exception){
            Log.e("NotifikasiUtils", "Error: ${e.message}")
            return Result.failure()
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(context: Context, title: String, message: String) {
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
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) // ganti dengan ikon notifikasi kamu
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }


    fun sendFCM(payload: JSONObject){
        val url = URL("https://fcm.googleapis.com/fcm/send")
        val conn = url.openConnection() as  HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "key=YOUR_SERVER_KEY")

        val outputWriter = OutputStreamWriter(conn.outputStream)
        outputWriter.write(payload.toString())
        outputWriter.flush()
        outputWriter.close()

        conn.responseCode
    }
}
