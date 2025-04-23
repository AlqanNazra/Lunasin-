package com.example.lunasin.Frontend.UI.Inputhutang.Qrcode

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.example.lunasin.MainActivity



class QrScannerActivity : AppCompatActivity() {
    private val qrScanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contents = result.data?.getStringExtra("SCAN_RESULT")
            contents?.let {
                val uri = Uri.parse(it)
                val docId = uri.getQueryParameter("docId")
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("navigateTo", "previewHutang/$docId")
                startActivity(intent)
                finish()
            }
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val integrator = IntentIntegrator(this)
        integrator.setOrientationLocked(false)
        qrScanLauncher.launch(integrator.createScanIntent())
    }
}
