package com.example.android.tvz.hr.mrrise

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class QRTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.puzzle_qr_code)
            Toast.makeText(this, "QR Layout loaded!", Toast.LENGTH_SHORT).show()

            val button = findViewById<android.widget.Button>(R.id.btnScanQR)
            if (button != null) {
                Toast.makeText(this, "Button found!", Toast.LENGTH_SHORT).show()
                button.setOnClickListener {
                    Toast.makeText(this, "Button clicked!", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Button NOT found!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}