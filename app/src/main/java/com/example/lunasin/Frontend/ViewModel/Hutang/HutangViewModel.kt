package com.example.lunasin.Frontend.ViewModel.Hutang

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lunasin.Backend.Service.management_BE.FirestoreService
import com.example.lunasin.Backend.Model.Hutang
import com.example.lunasin.Backend.Model.HutangType
import com.example.lunasin.Backend.Model.StatusBayar
import com.example.lunasin.R
import com.example.lunasin.utils.Notifikasi.NotificationReceiver
import com.example.lunasin.utils.NotifikasiUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HutangViewModel(private val firestoreService: FirestoreService) : ViewModel() {
    private val _hutangSayaList = MutableStateFlow<List<Hutang>>(emptyList())
    val hutangSayaList: StateFlow<List<Hutang>> = _hutangSayaList

    private val _hutangState = MutableStateFlow<Hutang?>(null)
    val hutangState: StateFlow<Hutang?> = _hutangState

    private val _recentSearch = MutableStateFlow<Hutang?>(null)
    val recentSearch: StateFlow<Hutang?> = _recentSearch

    private val _currentUserId = MutableStateFlow(FirebaseAuth.getInstance().currentUser?.uid.orEmpty())
    val currentUserId: StateFlow<String> = _currentUserId

    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun getHutangById(docId: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    Log.e("FirestoreError", "Pengguna tidak login")
                    onError("Harap login terlebih dahulu")
                    return@launch
                }
                Log.d("FirestoreDebug", "Mencari dokumen dengan docId: $docId untuk pengguna: ${currentUser.uid}")
                val document = firestore.collection("hutang").document(docId).get().await()
                if (document.exists()) {
                    val data = document.data ?: emptyMap()
                    Log.d("FirestoreDebug", "Dokumen ditemukan: ${document.id}, data: $data")
                    var hutang = Hutang.fromMap(data).copy(docId = document.id)
                    if (hutang.tanggalJatuhTempo.isBlank()) {
                        Log.e("FirestoreError", "Tanggal jatuh tempo kosong untuk docId: $docId")
                        onError("Data tidak valid: tanggal jatuh tempo kosong")
                        return@launch
                    }
                    if (hutang.totalDenda == 0.0 && hutang.hutangType == HutangType.PERHITUNGAN) {
                        Log.w("FirestoreWarning", "totalDenda adalah 0 untuk PERHITUNGAN, docId: $docId")
                    }
                    // Hitung totalHutang
                    val calculatedTotalHutang = HutangCalculator.hitungTotalHutang(hutang)
                    hutang = hutang.copy(totalHutang = calculatedTotalHutang)
                    // Update totalHutang di Firestore hanya jika BELUM_LUNAS dan keterlambatan > 0
                    if (hutang.statusBayar == StatusBayar.BELUM_LUNAS &&
                        HutangCalculator.hitungKeterlambatan(hutang.tanggalJatuhTempo) > 0 &&
                        calculatedTotalHutang != data["totalHutang"] as? Double
                    ) {
                        firestore.collection("hutang").document(docId)
                            .update("totalHutang", calculatedTotalHutang)
                            .addOnSuccessListener {
                                Log.d("FirestoreDebug", "totalHutang diperbarui di Firestore: $calculatedTotalHutang untuk docId: $docId")
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirestoreError", "Gagal memperbarui totalHutang untuk docId: $docId, error: ${e.message}")
                                onError("Gagal memperbarui data: ${e.message}")
                            }
                    }
                    _hutangState.value = hutang
                    _recentSearch.value = hutang
                    Log.d("FirestoreDebug", "Data hutang setelah perhitungan: docId=${hutang.docId}, totalDenda=${hutang.totalDenda}, totalHutang=${hutang.totalHutang}, tanggalJatuhTempo=${hutang.tanggalJatuhTempo}, statusBayar=${hutang.statusBayar}")
                } else {
                    _hutangState.value = null
                    _recentSearch.value = null
                    Log.w("Firestore", "Dokumen tidak ditemukan untuk docId: $docId")
                    onError("Dokumen tidak ditemukan")
                }
            } catch (e: Exception) {
                _hutangState.value = null
                _recentSearch.value = null
                Log.e("FirestoreError", "Gagal mengambil data: ${e.message}", e)
                if (e.message?.contains("PERMISSION_DENIED") == true) {
                    Log.w("SecurityWarning", "Akses ditolak untuk docId: $docId, pengguna: ${FirebaseAuth.getInstance().currentUser?.uid}")
                    onError("Anda tidak memiliki izin untuk mengakses data ini")
                } else {
                    onError("Gagal mengambil data: ${e.message}")
                }
            }
        }
    }

    fun ambilHutangSaya(userId: String) {
        viewModelScope.launch {
            try {
                val result = firestore.collection("hutang")
                    .whereEqualTo("id_penerima", userId)
                    .get()
                    .await()
                val batch = firestore.batch()
                val daftarHutang = result.documents.mapNotNull { doc ->
                    val data = doc.data ?: emptyMap()
                    Log.d("FirestoreDebug", "Dokumen dalam daftar: ${doc.id}, data: $data")
                    var hutang = Hutang.fromMap(data).copy(Id_Transaksi = doc.id)
                    if (hutang.tanggalJatuhTempo.isBlank()) {
                        Log.e("FirestoreError", "Tanggal jatuh tempo kosong untuk docId: ${doc.id}")
                        return@mapNotNull null
                    }
                    if (hutang.totalDenda == 0.0 && hutang.hutangType == HutangType.PERHITUNGAN) {
                        Log.w("FirestoreWarning", "totalDenda adalah 0 untuk PERHITUNGAN, docId: ${doc.id}")
                    }
                    // Hitung totalHutang
                    val calculatedTotalHutang = HutangCalculator.hitungTotalHutang(hutang)
                    hutang = hutang.copy(totalHutang = calculatedTotalHutang)
                    // Tambahkan update ke batch jika totalHutang berubah
                    if (calculatedTotalHutang != data["totalHutang"] as? Double) {
                        batch.update(
                            firestore.collection("hutang").document(doc.id),
                            "totalHutang", calculatedTotalHutang
                        )
                        Log.d("FirestoreDebug", "Menjadwalkan update totalHutang: $calculatedTotalHutang untuk docId: ${doc.id}")
                    }
                    hutang
                }
                // Commit batch update
                if (daftarHutang.isNotEmpty()) {
                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("FirestoreDebug", "Batch update totalHutang berhasil untuk ${daftarHutang.size} dokumen")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreError", "Gagal batch update totalHutang: ${e.message}")
                        }
                }
                _hutangSayaList.value = daftarHutang
                Log.d("HutangViewModel", "Daftar hutang setelah hitung: $daftarHutang")
            } catch (e: Exception) {
                Log.e("HutangViewModel", "Gagal ambil hutang: ${e.message}", e)
            }
        }
    }

    fun clearRecentSearch() {
        _recentSearch.value = null
        Log.d("HutangViewModel", "Recent search telah dihapus")
    }

    fun clearHutangState() {
        _hutangState.value = null
        Log.d("HutangViewModel", "Hasil pencarian telah dihapus")
    }

    fun klaimHutang(hutang: Hutang, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null || user.isAnonymous) {
                    Log.e("FirestoreError", "Pengguna tidak terautentikasi atau anonim")
                    onResult(false, "Harap login dengan akun yang valid")
                    return@launch
                }
                val userId = user.uid
                val hutangRef = firestore.collection("hutang").document(hutang.docId)

                // Periksa keberadaan dokumen
                val docSnapshot = hutangRef.get().await()
                if (!docSnapshot.exists()) {
                    Log.e("FirestoreError", "Dokumen tidak ditemukan: ${hutang.docId}")
                    onResult(false, "Hutang tidak ditemukan")
                    return@launch
                }

                Log.d("FirestoreDebug", "Mengklaim hutang: docId=${hutang.docId}, id_penerima=$userId")
                hutangRef.update(
                    mapOf(
                        "isClaimed" to true,
                        "claimedBy" to userId,
                        "id_penerima" to userId
                    )
                ).await()
                Log.d("FirestoreDebug", "Hutang berhasil diklaim oleh: $userId")
                onResult(true, null)
            } catch (e: Exception) {
                Log.e("FirestoreError", "Gagal mengklaim hutang: ${e.message}", e)
                if (e.message?.contains("PERMISSION_DENIED") == true) {
                    onResult(false, "Anda tidak memiliki izin untuk mengklaim hutang ini")
                } else {
                    onResult(false, e.message)
                }
            }
        }
    }

    fun hitungDanSimpanHutang(
        type: String,
        hutangType: HutangType,
        namapinjaman: String,
        nominalpinjaman: Double,
        bunga: Double, // Denda tetap untuk PERHITUNGAN, 0 untuk TEMAN
        lamaPinjam: Int,
        tanggalPinjam: String,
        tanggalJatuhTempo: String? = null,
        catatan: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.e("FirestoreError", "User belum login, tidak bisa menyimpan hutang")
            onResult(false, null)
            return
        }

        val userId = user.uid
        Log.d("FirestoreDebug", "UID pengguna: $userId")
        if (hutangType == HutangType.PERHITUNGAN) {
            Log.d("FirestoreDebug", "Denda tetap untuk PERHITUNGAN: $bunga")
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Validasi tanggalPinjam
        val parsedDate = try {
            sdf.parse(tanggalPinjam)
        } catch (e: Exception) {
            Log.e("FirestoreError", "Format tanggal pinjaman tidak valid: $tanggalPinjam", e)
            onResult(false, null)
            return
        }
        if (parsedDate == null) {
            Log.e("FirestoreError", "Gagal mengonversi tanggal pinjaman: $tanggalPinjam")
            onResult(false, null)
            return
        }

        // Validasi tanggalJatuhTempo untuk TEMAN dan PERHITUNGAN
        val effectiveTanggalJatuhTempo = when (hutangType) {
            HutangType.TEMAN, HutangType.PERHITUNGAN -> {
                if (tanggalJatuhTempo.isNullOrEmpty()) {
                    Log.e("FirestoreError", "Tanggal jatuh tempo wajib untuk tipe $hutangType")
                    onResult(false, null)
                    return
                }
                try {
                    sdf.parse(tanggalJatuhTempo)
                    tanggalJatuhTempo
                } catch (e: Exception) {
                    Log.e("FirestoreError", "Format tanggal jatuh tempo tidak valid: $tanggalJatuhTempo", e)
                    onResult(false, null)
                    return
                }
            }
        }

        val idTransaksi = generateIdHutang()
        val calendar = Calendar.getInstance().apply { time = parsedDate }

        val hutangData = when (hutangType) {
            HutangType.TEMAN -> createHutangTemanData(
                idTransaksi, userId, namapinjaman, nominalpinjaman, tanggalPinjam, effectiveTanggalJatuhTempo, catatan
            )
            HutangType.PERHITUNGAN -> createHutangPerhitunganData(
                idTransaksi, userId, namapinjaman, nominalpinjaman, bunga, tanggalPinjam, effectiveTanggalJatuhTempo, catatan
            )
        }

        val finalHutangData = hutangData.toMutableMap().apply {
            put("type", type)
        }.filterValues { it != null } as Map<String, Any>

        Log.d("FirestoreDebug", "Data yang akan disimpan: $finalHutangData")
        firestore.collection("hutang").add(finalHutangData)
            .addOnSuccessListener { documentReference ->
                val docId = documentReference.id
                Log.d("FirestoreDebug", "Dokumen berhasil disimpan dengan ID: $docId")
                documentReference.update("docId", docId)
                    .addOnSuccessListener {
                        Log.d("FirestoreSuccess", "Hutang berhasil disimpan dengan docId: $docId")
                        // Hitung ulang totalHutang setelah simpan
                        val hutang = Hutang.fromMap(finalHutangData).copy(docId = docId)
                        val calculatedTotalHutang = HutangCalculator.hitungTotalHutang(hutang)
                        if (calculatedTotalHutang != finalHutangData["totalHutang"] as? Double) {
                            documentReference.update("totalHutang", calculatedTotalHutang)
                                .addOnSuccessListener {
                                    Log.d("FirestoreDebug", "totalHutang awal diperbarui: $calculatedTotalHutang untuk docId: $docId")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FirestoreError", "Gagal memperbarui totalHutang awal: ${e.message}")
                                }
                        }
                        onResult(true, docId)
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreError", "Gagal memperbarui docId untuk hutang: ${e.message}", e)
                        onResult(false, null)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Gagal menyimpan data hutang: ${e.message}", e)
                Log.d("FirestoreDebug", "Data yang gagal disimpan: $finalHutangData")
                onResult(false, null)
            }
    }

    private fun createHutangTemanData(
        idTransaksi: String,
        userId: String,
        namapinjaman: String,
        nominalpinjaman: Double,
        tanggalPinjam: String,
        tanggalJatuhTempo: String,
        catatan: String
    ): Map<String, Any?> {
        val totalHutang = nominalpinjaman
        return mapOf(
            "Id_Transaksi" to idTransaksi,
            "userId" to userId,
            "namapinjaman" to namapinjaman,
            "nominalpinjaman" to nominalpinjaman,
            "totalHutang" to totalHutang,
            "totalDenda" to 0.0,
            "tanggalPinjam" to tanggalPinjam,
            "tanggalJatuhTempo" to tanggalJatuhTempo,
            "catatan" to catatan,
            "id_penerima" to "",
            "hutangType" to HutangType.TEMAN.name,
            "statusBayar" to StatusBayar.BELUM_LUNAS.name
        )
    }

    private fun createHutangPerhitunganData(
        idTransaksi: String,
        userId: String,
        namapinjaman: String,
        nominalpinjaman: Double,
        dendaTetap: Double,
        tanggalPinjam: String,
        tanggalJatuhTempo: String,
        catatan: String
    ): Map<String, Any?> {
        val totalHutang = nominalpinjaman // Denda tidak ditambahkan saat menyimpan awal
        Log.d("FirestoreDebug", "Menyimpan denda tetap untuk PERHITUNGAN: $dendaTetap")
        return mapOf(
            "Id_Transaksi" to idTransaksi,
            "userId" to userId,
            "namapinjaman" to namapinjaman,
            "nominalpinjaman" to nominalpinjaman,
            "totalDenda" to dendaTetap,
            "totalHutang" to totalHutang,
            "tanggalPinjam" to tanggalPinjam,
            "tanggalJatuhTempo" to tanggalJatuhTempo,
            "catatan" to catatan,
            "id_penerima" to "",
            "hutangType" to HutangType.PERHITUNGAN.name,
            "statusBayar" to StatusBayar.BELUM_LUNAS.name
        )
    }

    private fun generateIdHutang(): String {
        val timestamp = SimpleDateFormat("ddMMyyyyHHmmss", Locale.getDefault()).format(Date())
        val randomString = (1..5)
            .map { ('A'..'Z') + ('0'..'9') }
            .flatten()
            .shuffled()
            .take(5)
            .joinToString("")
        return "${timestamp}_$randomString"
    }

    suspend fun uploadBuktiPembayaran(hutangId: String, imageUri: Uri): String? {
        return try {
            val storageRef = storage.reference.child("bukti_pembayaran/$hutangId.jpg")
            val uploadTask = storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            // Update Firestore dengan URL foto dan status LUNAS
            val hutangRef = firestore.collection("hutang").document(hutangId)
            val document = hutangRef.get().await()
            if (document.exists()) {
                val data = document.data ?: emptyMap()
                val hutang = Hutang.fromMap(data).copy(docId = hutangId)
                // Set totalHutang ke nominalpinjaman karena status menjadi LUNAS
                hutangRef.update(
                    mapOf(
                        "buktiPembayaranUrl" to downloadUrl,
                        "statusBayar" to StatusBayar.LUNAS.name,
                        "tanggalBayar" to LocalDate.now().toString(),
                        "totalHutang" to hutang.nominalpinjaman // Reset totalHutang
                    )
                ).await()
                Log.d("FirestoreDebug", "Bukti pembayaran diunggah dan totalHutang direset ke ${hutang.nominalpinjaman} untuk docId: $hutangId")
            }
            downloadUrl
        } catch (e: Exception) {
            Log.e("HutangViewModel", "Error uploading bukti pembayaran: ${e.message}")
            null
        }
    }

    fun scheduleDailyNotification(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set waktu notifikasi berdasarkan input
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun showImmediateNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_reminder"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Reminder",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Test Reminder")
            .setContentText("Ini adalah notifikasi test dari Lunasin")
            .setSmallIcon(R.drawable.ic_notification)
            .build()

        notificationManager.notify(1002, notification)
    }
}