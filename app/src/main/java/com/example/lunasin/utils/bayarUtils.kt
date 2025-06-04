package com.example.lunasin.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.lunasin.Backend.Model.StatusBayar
import com.example.lunasin.Frontend.ViewModel.Hutang.HutangViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class bayarUtils {
    fun updatePaymentProof(docId: String, url: String) {
        FirebaseFirestore.getInstance()
            .collection("hutang")
            .document(docId)
            .update("buktiPembayaran", url)
            .addOnFailureListener { e ->
                Log.e("HutangViewModel", "Error updating buktiPembayaran: ${e.message}")
            }
    }

    fun sharePaymentProof(context: Context, filePath: String) {
        val file = File(filePath)
        val authority = "com.example.lunasin.fileprovider"

        val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "image/jpeg")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Buka bukti pembayaran dengan"))
    }

    suspend fun uploadPaymentProof(file: File, docId: String): String? {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
            val fileRef = storageRef.child("payment_proofs/$docId/${file.name}")
            fileRef.putFile(Uri.fromFile(file)).await()
            val downloadUrl = fileRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e("UploadPaymentProof", "Error: ${e.message}")
            null
        }
    }

    suspend fun updatePaymentStatus(docId: String): Boolean {
        return try {
            FirebaseFirestore.getInstance()
                .collection("hutang")
                .document(docId)
                .update("statusBayar", StatusBayar.LUNAS.name)
                .await()
            true
        } catch (e: Exception) {
            Log.e("UpdatePaymentStatus", "Error updating statusBayar: ${e.message}")
            false
        }
    }

    suspend fun handleContentSelection(
        context: Context,
        uri: Uri?,
        docId: String,
        viewModel: HutangViewModel
    ) {
        uri?.let {
            // Simpan file ke cache
            val file = File(context.cacheDir, "photo${System.currentTimeMillis()}.jpg")
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                // Unggah ke Firebase Storage
                val url = uploadPaymentProof(file, docId)
                url?.let {
                    updatePaymentProof(docId, it)
                    viewModel.getHutangById(docId) { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                    // Hapus file sementara
                    file.delete()
                } ?: run {
                    Toast.makeText(context, "Gagal mengunggah bukti pembayaran", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PaymentProof", "Error processing image: ${e.message}")
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun handleCameraCapture(
        context: Context,
        success: Boolean,
        imageUri: Uri?,
        docId: String,
        viewModel: HutangViewModel,
        isNetworkAvailable: (Context) -> Boolean,
        onUploadingChange: (Boolean) -> Unit
    ) {
        if (success && imageUri != null) {
            if (!isNetworkAvailable(context)) {
                onUploadingChange(false)
                Toast.makeText(context, "Tidak ada koneksi jaringan", Toast.LENGTH_SHORT).show()
                return
            }

            onUploadingChange(true)
            val fileName = "images/${UUID.randomUUID()}.jpg"
            val storageRef = FirebaseStorage.getInstance().reference.child(fileName)

            try {
                storageRef.putFile(imageUri).await()
                val downloadUri = storageRef.downloadUrl.await()
                updatePaymentProof(docId, downloadUri.toString())
                viewModel.getHutangById(docId) { errorMessage ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
                onUploadingChange(false)
                Toast.makeText(context, "Bukti pembayaran berhasil diunggah!", Toast.LENGTH_SHORT).show()
                // Hapus file sementara
                File(imageUri.path!!).delete()
            } catch (exception: Exception) {
                onUploadingChange(false)
                when (exception) {
                    is StorageException -> {
                        when (exception.errorCode) {
                            StorageException.ERROR_OBJECT_NOT_FOUND -> {
                                Log.e("Storage", "Objek tidak ditemukan di lokasi")
                                Toast.makeText(context, "Objek tidak ditemukan", Toast.LENGTH_SHORT).show()
                            }
                            StorageException.ERROR_UNKNOWN -> {
                                Log.e("Storage", "Kesalahan tidak diketahui: ${exception.message}")
                                Toast.makeText(context, "Kesalahan tidak diketahui", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Log.e("Storage", "Gagal mengunggah: ${exception.message}")
                                Toast.makeText(context, "Gagal mengunggah: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    else -> {
                        Log.e("Storage", "Error saat mengunggah: ${exception.message}")
                        Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            onUploadingChange(false)
            Toast.makeText(context, "Gagal mengambil gambar", Toast.LENGTH_SHORT).show()
        }
    }
}