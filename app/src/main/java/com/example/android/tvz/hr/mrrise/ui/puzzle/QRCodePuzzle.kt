package com.example.android.tvz.hr.mrrise.ui.puzzle

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.example.android.tvz.hr.mrrise.R
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class QRCodePuzzle(
    private val container: ViewGroup,
    private val activity: androidx.appcompat.app.AppCompatActivity,
    private val onComplete: () -> Unit
) {

    private val view: View = LayoutInflater.from(container.context).inflate(
        R.layout.puzzle_qr_code,
        container,
        true
    )

    private val btnScanQR: Button = view.findViewById(R.id.btnScanQR)
    private val tvStatus: TextView = view.findViewById(R.id.tvStatus)

    private val barcodeLauncher: ActivityResultLauncher<ScanOptions> =
        activity.registerForActivityResult(ScanContract()) { result ->
            if (result.contents != null) {
                handleQRScan(result.contents)
            } else {
                tvStatus.text = view.context.getString(R.string.scan_cancelled)
                tvStatus.setTextColor(Color.RED)
            }
        }

    init {
        setupScanButton()
    }

    private fun setupScanButton() {
        btnScanQR.setOnClickListener {
            checkCameraPermissionAndScan()
        }
    }

    private fun checkCameraPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startQRScanner()
        } else {
            activity.requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun startQRScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt(view.context.getString(R.string.scan_qr_dismiss))
            setBeepEnabled(true)
            setOrientationLocked(true)  // ← Changed to true
        }
        barcodeLauncher.launch(options)
    }

    private fun handleQRScan(content: String) {
        tvStatus.text = view.context.getString(R.string.qr_scanned)
        tvStatus.setTextColor(Color.parseColor("#2ECC71"))
        btnScanQR.isEnabled = false

        view.postDelayed({
            onComplete()
        }, 1000)
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            startQRScanner()
        } else {
            tvStatus.text = view.context.getString(R.string.camera_permission_required)
            tvStatus.setTextColor(Color.RED)
        }
    }

    companion object {
        const val CAMERA_PERMISSION_CODE = 100
    }
}